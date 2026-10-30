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
import dev.vality.fraudbusters.fraud.payment.PaymentContextParserImpl;
import dev.vality.fraudbusters.fraud.payment.resolver.PaymentModelFieldResolver;
import dev.vality.fraudbusters.listener.payment.TemplateListener;
import dev.vality.fraudbusters.pool.PoolImpl;
import dev.vality.fraudbusters.util.CheckedResultFactory;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PayoutInspectionTest {

    private final PayoutRulePools pools = new PayoutRulePools();
    private final PayoutCommandHandler commands = new PayoutCommandHandler(pools, new PaymentContextParserImpl());
    private final PayoutAggregators payouts = mock(PayoutAggregators.class);
    private final WithdrawalContextToModelConverter converter = new WithdrawalContextToModelConverter();
    private final PayoutInspectionService inspector = new PayoutInspectionService(pools,
            new PayoutFraudoConfig().payoutRuleVisitor(null, null, null, null, null,
                    new PaymentModelFieldResolver(), payouts), new CheckedResultFactory(), converter);
    private final CheckedResultToRiskScoreConverter risk = new CheckedResultToRiskScoreConverter();

    @Test
    void damselMethodUsesPayoutInspectionWithoutPublishingPaymentResults() throws Exception {
        var producer = mock(org.springframework.kafka.core.KafkaTemplate.class);
        var handler = new dev.vality.fraudbusters.resource.payment.handler.FraudInspectorHandler(
                "unused", risk, null, null, producer, null, inspector);
        assertEquals(RiskScore.high, handler.inspectWithdrawal(context()));
        template("global", "rule:1=1->decline;");
        commands.handle(create(CommandBody.reference(new TemplateReference().setTemplateId("global")
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
        assertEquals(RiskScore.high, risk.convert(inspector.inspect(context)));
    }

    @Test
    void walletRuleCanQueryPayoutHistory() {
        template("shared-id", "rule:countPayoutsSuccess(\"user_id\",24,hours,\"party_id\")>2->decline;");
        reference("shared-id", "synthetic-party", "synthetic-wallet");
        when(payouts.countSuccess(eq(USER_ID), any(), any(), anyList())).thenReturn(3);
        assertEquals(RiskScore.fatal, risk.convert(inspector.inspect(context())));
        verify(payouts).countSuccess(eq(USER_ID), any(), any(), anyList());
        commands.handle(new Command().setCommandType(CommandType.DELETE).setCommandBody(CommandBody.template(
                new Template().setId("shared-id").setOperationType(OperationType.payout))));
        assertEquals(RiskScore.high, risk.convert(inspector.inspect(context())));
    }

    @Test
    void notificationsContinueToLaterRulesAndMapToLowWhenAlone() {
        template("global", "rule:1=1->notify;");
        commands.handle(create(CommandBody.reference(new TemplateReference().setTemplateId("global")
                .setIsGlobal(true).setOperationType(OperationType.payout))));
        assertEquals(RiskScore.low, risk.convert(inspector.inspect(context())));
        template("wallet", "rule:1=1->highRisk;");
        reference("wallet", "synthetic-party", "synthetic-wallet");
        var checked = inspector.inspect(context());
        assertEquals(RiskScore.high, risk.convert(checked));
        assertFalse(checked.getResultModel().getNotificationsRule().isEmpty());
    }

    @Test
    void groupsAreOrderedAndGlobalRulesTakePrecedence() {
        template("accept", "rule:1=1->accept;");
        template("decline", "rule:1=1->decline;");
        commands.handle(create(CommandBody.group(new Group().setGroupId("group")
                .setOperationType(OperationType.payout).setTemplateIds(List.of(
                        new PriorityId(2, "decline"), new PriorityId(1, "accept"))))));
        commands.handle(create(CommandBody.group_reference(new GroupReference().setGroupId("group")
                .setPartyId("synthetic-party").setOperationType(OperationType.payout))));
        assertEquals(RiskScore.low, risk.convert(inspector.inspect(context())));
        commands.handle(create(CommandBody.reference(new TemplateReference().setTemplateId("decline")
                .setIsGlobal(true).setOperationType(OperationType.payout))));
        assertEquals(RiskScore.fatal, risk.convert(inspector.inspect(context())));
    }

    @Test
    void payoutCommandsDoNotChangePaymentTemplatesAndInvalidTemplatesAreRejected() {
        var payments = new PoolImpl<org.antlr.v4.runtime.ParserRuleContext>("payment-test");
        var parser = new PaymentContextParserImpl();
        var original = parser.parse("rule:1=1->accept;");
        payments.add("shared-id", original);
        var listener = new TemplateListener(commands, parser,
                new dev.vality.fraudbusters.fraud.payment.validator.PaymentTemplateValidator(), payments);
        listener.listen(create(CommandBody.template(new Template().setId("shared-id")
                .setOperationType(OperationType.payout).setTemplate(bytes("rule:1=1->decline;")))));
        assertSame(original, payments.get("shared-id"));
        assertNotNull(pools.getTemplates().get("shared-id"));
        assertThrows(IllegalArgumentException.class, () -> template("invalid", "rule:1=1->3ds;"));
        assertNull(pools.getTemplates().get("invalid"));
        assertThrows(IllegalArgumentException.class, () -> commands.handle(create(CommandBody.reference(
                new TemplateReference().setTemplateId("shared-id").setOperationType(OperationType.payout)
                        .setPartyId("synthetic-party").setShopId("synthetic-shop")))));
    }

    @Test
    void invalidGroupReplacementKeepsThePreviousGroup() {
        var group = new Group().setGroupId("group").setOperationType(OperationType.payout)
                .setTemplateIds(List.of(new PriorityId(1, "old")));
        commands.handle(create(CommandBody.group(group)));
        group.setTemplateIds(List.of(new PriorityId(1, "duplicate"), new PriorityId(2, "duplicate")));
        assertThrows(IllegalArgumentException.class, () -> commands.handle(create(CommandBody.group(group))));
        assertEquals(List.of("old"), pools.getGroups().get("group"));
    }

    private void template(String id, String source) {
        commands.handle(create(CommandBody.template(new Template().setId(id).setTemplate(bytes(source))
                .setOperationType(OperationType.payout))));
    }

    private void reference(String id, String party, String wallet) {
        commands.handle(create(CommandBody.reference(new TemplateReference().setTemplateId(id)
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
