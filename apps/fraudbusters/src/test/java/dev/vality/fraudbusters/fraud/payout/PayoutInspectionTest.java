package dev.vality.fraudbusters.fraud.payout;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.ClientIdentity;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PartyConfigRef;
import dev.vality.damsel.domain.RiskScore;
import dev.vality.damsel.domain.WalletConfigRef;
import dev.vality.damsel.fraudbusters.*;
import dev.vality.damsel.proxy_inspector.Party;
import dev.vality.damsel.proxy_inspector.WithdrawalContext;
import dev.vality.damsel.withdrawals.domain.Destination;
import dev.vality.fraudbusters.config.payout.PayoutFraudoConfig;
import dev.vality.fraudbusters.converter.CheckedResultToRiskScoreConverter;
import dev.vality.fraudbusters.converter.WithdrawalContextToModelConverter;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.fraud.InspectStorageRegistry;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payment.PaymentContextParserImpl;
import dev.vality.fraudbusters.fraud.payment.PaymentInspectStorage;
import dev.vality.fraudbusters.fraud.payment.resolver.PaymentModelFieldResolver;
import dev.vality.fraudbusters.fraud.payment.validator.PaymentTemplateValidator;
import dev.vality.fraudbusters.listener.payment.GroupListener;
import dev.vality.fraudbusters.listener.payment.GroupReferenceListener;
import dev.vality.fraudbusters.listener.payment.TemplateListener;
import dev.vality.fraudbusters.listener.payment.TemplateReferenceListener;
import dev.vality.fraudbusters.pool.Pool;
import dev.vality.fraudbusters.pool.PoolImpl;
import dev.vality.fraudbusters.stream.impl.PayoutTemplateVisitorImpl;
import dev.vality.fraudbusters.stream.impl.NotifyingRuleApplierImpl;
import dev.vality.fraudbusters.util.CheckedResultFactory;
import dev.vality.fraudo.payment.aggregator.CountPaymentAggregator;
import dev.vality.fraudo.payment.visitor.impl.FirstFindVisitorImpl;
import org.antlr.v4.runtime.ParserRuleContext;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PayoutInspectionTest {

    private final PaymentContextParserImpl parser = new PaymentContextParserImpl();
    private final Pool<ParserRuleContext> paymentTemplates = new PoolImpl<>("template");
    private final Pool<ParserRuleContext> payoutTemplates = new PoolImpl<>("payout-template");
    private final Pool<List<String>> payoutGroups = new PoolImpl<>("payout-group");
    private final Pool<String> payoutReferences = new PoolImpl<>("payout-reference");
    private final Pool<String> payoutGroupReferences = new PoolImpl<>("payout-group-reference");
    private final InspectStorageRegistry registry = new InspectStorageRegistry(List.of(
            new PaymentInspectStorage(paymentTemplates, new PoolImpl<>("group"), new PoolImpl<>("reference"),
                    new PoolImpl<>("group-reference"), new PaymentTemplateValidator()),
            new PayoutInspectStorage(payoutTemplates, payoutGroups, payoutReferences, payoutGroupReferences,
                    new PayoutFraudoConfig().payoutTemplateValidator())));
    private final TemplateListener templateListener = new TemplateListener(parser, registry);
    private final GroupListener groupListener = new GroupListener(registry);
    private final TemplateReferenceListener referenceListener = new TemplateReferenceListener(registry);
    private final GroupReferenceListener groupReferenceListener = new GroupReferenceListener(registry);
    private final PayoutAggregators payouts = mock(PayoutAggregators.class);
    private final WithdrawalContextToModelConverter converter = new WithdrawalContextToModelConverter();
    private final PayoutTemplateVisitorImpl inspector = inspector(new PayoutFraudoConfig().payoutRuleVisitor(
            null, null, null, null, null, new PaymentModelFieldResolver(), payouts));
    private final CheckedResultToRiskScoreConverter risk = new CheckedResultToRiskScoreConverter();

    @Test
    void damselMethodUsesPayoutInspectionWithoutPublishingPaymentResults() throws Exception {
        var producer = mock(org.springframework.kafka.core.KafkaTemplate.class);
        var handler = new dev.vality.fraudbusters.resource.payment.handler.FraudInspectorHandler(
                "unused", risk, null, null, producer, null, converter, inspector);
        assertEquals(RiskScore.high, handler.inspectWithdrawal(context()));
        template("global", "rule:1=1->decline;");
        handle(create(CommandBody.reference(new TemplateReference().setTemplateId("global")
                .setIsGlobal(true).setOperationType(OperationType.payout))));
        assertEquals(RiskScore.fatal, handler.inspectWithdrawal(context()));
        verifyNoInteractions(producer);
    }

    @Test
    void noDecisionIsHighAndCustomerIsNotUsedAsUser() {
        var context = context();
        var model = converter.convert(context);
        assertNull(model.getUserId());
        assertNull(model.getShopId());
        assertEquals("synthetic-wallet", model.getWalletId());
        assertEquals(OperationType.payout, model.getOperationType());
        assertEquals(RiskScore.high, risk.convert(inspect(context)));
    }

    @Test
    void walletRuleCanQueryPayoutHistory() {
        template("shared-id", "rule:countPayoutsSuccess(\"user_id\",24,hours,\"party_id\")>2->decline;");
        reference("shared-id", "synthetic-party", "synthetic-wallet");
        when(payouts.countSuccess(eq(USER_ID), any(), any(), anyList())).thenReturn(3);
        assertEquals(RiskScore.fatal, risk.convert(inspect(context())));
        verify(payouts).countSuccess(eq(USER_ID), any(), any(), anyList());
        handle(new Command().setCommandType(CommandType.DELETE).setCommandBody(CommandBody.template(
                new Template().setId("shared-id").setOperationType(OperationType.payout))));
        assertEquals(RiskScore.high, risk.convert(inspect(context())));
    }

    @Test
    void notificationsContinueToLaterRulesAndMapToLowWhenAlone() {
        template("global", "rule:1=1->notify;");
        handle(create(CommandBody.reference(new TemplateReference().setTemplateId("global")
                .setIsGlobal(true).setOperationType(OperationType.payout))));
        assertEquals(RiskScore.low, risk.convert(inspect(context())));
        template("wallet", "rule:1=1->highRisk;");
        reference("wallet", "synthetic-party", "synthetic-wallet");
        var checked = inspect(context());
        assertEquals(RiskScore.high, risk.convert(checked));
        assertFalse(checked.getResultModel().getNotificationsRule().isEmpty());
    }

    @Test
    void groupNotificationsAreKeptWithTheLaterDecision() {
        template("notify", "rule:notify_rule:1=1->notify;");
        template("decline", "rule:1=1->decline;");
        handle(create(CommandBody.group(new Group().setGroupId("group")
                .setOperationType(OperationType.payout).setTemplateIds(List.of(
                        new PriorityId(1, "notify"), new PriorityId(2, "decline"))))));
        handle(create(CommandBody.group_reference(new GroupReference().setGroupId("group")
                .setPartyId("synthetic-party").setOperationType(OperationType.payout))));
        var checked = inspect(context());
        assertEquals(RiskScore.fatal, risk.convert(checked));
        assertEquals("decline", checked.getCheckedTemplate());
        assertEquals(List.of("notify_rule"), checked.getResultModel().getNotificationsRule());
    }

    @Test
    void groupsAreOrderedAndGlobalRulesTakePrecedence() {
        template("accept", "rule:1=1->accept;");
        template("decline", "rule:1=1->decline;");
        handle(create(CommandBody.group(new Group().setGroupId("group")
                .setOperationType(OperationType.payout).setTemplateIds(List.of(
                        new PriorityId(2, "decline"), new PriorityId(1, "accept"))))));
        handle(create(CommandBody.group_reference(new GroupReference().setGroupId("group")
                .setPartyId("synthetic-party").setOperationType(OperationType.payout))));
        assertEquals(RiskScore.low, risk.convert(inspect(context())));
        handle(create(CommandBody.reference(new TemplateReference().setTemplateId("decline")
                .setIsGlobal(true).setOperationType(OperationType.payout))));
        assertEquals(RiskScore.fatal, risk.convert(inspect(context())));
    }

    @Test
    void payoutCommandsDoNotChangePaymentTemplatesAndInvalidTemplatesAreRejected() {
        handle(create(CommandBody.template(new Template().setId("shared-id")
                .setTemplate(bytes("rule:1=1->accept;")))));
        var original = paymentTemplates.get("shared-id");
        assertNotNull(original);
        assertNull(payoutTemplates.get("shared-id"));
        template("shared-id", "rule:1=1->decline;");
        assertSame(original, paymentTemplates.get("shared-id"));
        assertNotNull(payoutTemplates.get("shared-id"));
        assertThrows(IllegalArgumentException.class, () -> template("invalid", "rule:1=1->3ds;"));
        assertNull(payoutTemplates.get("invalid"));
        assertThrows(IllegalArgumentException.class, () -> handle(create(CommandBody.reference(
                new TemplateReference().setTemplateId("shared-id").setOperationType(OperationType.payout)
                        .setPartyId("synthetic-party").setShopId("synthetic-shop")))));
    }

    @Test
    @SuppressWarnings("unchecked")
    void mixedHistoryCachesRemainSeparateAndAreClearedBetweenInspections() {
        CountPaymentAggregator<PaymentModel, PaymentCheckedField> payments = mock(CountPaymentAggregator.class);
        var visitor = new PayoutFraudoConfig().payoutRuleVisitor(payments, null, null, null, null,
                new PaymentModelFieldResolver(), payouts);
        final var service = inspector(visitor);
        template("mixed", """
                rule: countSuccess("user_id",24,hours)=1
                  AND countPayoutsSuccess("user_id",24,hours)=3
                  AND countSuccess("user_id",24,hours)=1
                  AND countPayoutsSuccess("user_id",24,hours)=3 -> decline;
                """);
        reference("mixed", "synthetic-party", "synthetic-wallet");
        when(payments.countSuccess(eq(USER_ID), any(), any(), anyList())).thenReturn(1);
        when(payouts.countSuccess(eq(USER_ID), any(), any(), anyList())).thenReturn(3);
        assertEquals(RiskScore.fatal, risk.convert(inspect(service, context())));
        verify(payments).countSuccess(eq(USER_ID), any(), any(), anyList());
        verify(payouts).countSuccess(eq(USER_ID), any(), any(), anyList());
        assertEquals(RiskScore.fatal, risk.convert(inspect(service, context())));
        verify(payments, times(2)).countSuccess(eq(USER_ID), any(), any(), anyList());
        verify(payouts, times(2)).countSuccess(eq(USER_ID), any(), any(), anyList());
    }

    private PayoutTemplateVisitorImpl inspector(FirstFindVisitorImpl<PaymentModel, PaymentCheckedField> visitor) {
        return new PayoutTemplateVisitorImpl(
                new NotifyingRuleApplierImpl<>(visitor, payoutTemplates, new CheckedResultFactory()),
                payoutGroups, payoutReferences, payoutGroupReferences);
    }

    private CheckedResultModel inspect(WithdrawalContext context) {
        return inspect(inspector, context);
    }

    private CheckedResultModel inspect(PayoutTemplateVisitorImpl visitor, WithdrawalContext context) {
        return visitor.visit(converter.convert(context));
    }

    /** Routes a command to the listener of its Kafka topic. */
    private void handle(Command command) {
        var body = command.getCommandBody();
        if (body.isSetTemplate()) {
            templateListener.listen(command);
        } else if (body.isSetGroup()) {
            groupListener.listen(command);
        } else if (body.isSetReference()) {
            referenceListener.listen(command);
        } else {
            groupReferenceListener.listen(command);
        }
    }

    private void template(String id, String source) {
        handle(create(CommandBody.template(new Template().setId(id).setTemplate(bytes(source))
                .setOperationType(OperationType.payout))));
    }

    private void reference(String id, String party, String wallet) {
        handle(create(CommandBody.reference(new TemplateReference().setTemplateId(id)
                .setPartyId(party).setWalletId(wallet).setOperationType(OperationType.payout))));
    }

    private Command create(CommandBody body) {
        return new Command().setCommandType(CommandType.CREATE).setCommandBody(body);
    }

    private byte[] bytes(String value) {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    private WithdrawalContext context() {
        return new WithdrawalContext().setParty(new Party(new PartyConfigRef("synthetic-party")))
                .setWalletRef(new WalletConfigRef("synthetic-wallet")).setWithdrawalId("synthetic-withdrawal")
                .setWithdrawal(new dev.vality.damsel.withdrawals.domain.Withdrawal()
                        .setBody(new Cash(100, new CurrencyRef("RUB")))
                        .setDestination(Destination.digital_wallet(new dev.vality.damsel.domain.DigitalWallet()
                                .setId("synthetic-destination")))
                        .setCreatedAt("2026-01-01T12:00:00Z")
                        .setClientIdentity(new ClientIdentity().setCustomerId("synthetic-customer")));
    }
}
