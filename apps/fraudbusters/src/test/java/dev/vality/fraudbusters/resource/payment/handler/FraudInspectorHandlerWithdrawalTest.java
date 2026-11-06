package dev.vality.fraudbusters.resource.payment.handler;

import dev.vality.damsel.base.InvalidRequest;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.DigitalWallet;
import dev.vality.damsel.domain.PartyConfigRef;
import dev.vality.damsel.domain.WalletConfigRef;
import dev.vality.damsel.proxy_inspector.Party;
import dev.vality.damsel.proxy_inspector.WithdrawalContext;
import dev.vality.damsel.withdrawals.domain.Destination;
import dev.vality.damsel.withdrawals.domain.Withdrawal;
import dev.vality.fraudbusters.converter.CheckedResultToRiskScoreConverter;
import dev.vality.fraudbusters.converter.WithdrawalContextToModelConverter;
import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.pool.PoolImpl;
import dev.vality.fraudbusters.stream.RuleApplier;
import dev.vality.fraudbusters.stream.TemplateVisitor;
import dev.vality.fraudbusters.stream.impl.PayoutTemplateVisitorImpl;
import dev.vality.fraudbusters.stream.impl.RuleCascade;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class FraudInspectorHandlerWithdrawalTest {

    @SuppressWarnings("unchecked")
    private final KafkaTemplate<String, PayoutResult> payoutResults = mock(KafkaTemplate.class);
    @SuppressWarnings("unchecked")
    private final RuleApplier<PayoutModel> rules = mock(RuleApplier.class);

    @Test
    void withdrawalWithoutPartyOrWalletIsAnInvalidRequestAndIsNotPublished() {
        var handler = handler(new PayoutTemplateVisitorImpl(new RuleCascade<>(rules, new PoolImpl<>("group"),
                new PoolImpl<>("reference"), new PoolImpl<>("group-reference"))));

        InvalidRequest withoutWallet = assertThrows(InvalidRequest.class,
                () -> handler.inspectWithdrawal(context("synthetic-party", "")));
        assertFalse(withoutWallet.getErrors().isEmpty());
        assertThrows(InvalidRequest.class, () -> handler.inspectWithdrawal(context(" ", "synthetic-wallet")));
        verifyNoInteractions(payoutResults, rules);
    }

    @Test
    void unexpectedInspectionFailureIsReportedAndNotPublished() {
        @SuppressWarnings("unchecked")
        TemplateVisitor<PayoutModel, CheckedResultModel> failing = mock(TemplateVisitor.class);
        when(failing.visit(any())).thenThrow(new IllegalStateException("synthetic failure"));
        var handler = handler(failing);

        TException error = assertThrows(TException.class,
                () -> handler.inspectWithdrawal(context("synthetic-party", "synthetic-wallet")));
        assertEquals(IllegalStateException.class, error.getCause().getClass());
        verifyNoInteractions(payoutResults);
    }

    @Test
    void contextWithoutRequiredFieldsIsRejectedBeforeInspection() {
        @SuppressWarnings("unchecked")
        TemplateVisitor<PayoutModel, CheckedResultModel> visitor = mock(TemplateVisitor.class);
        var handler = handler(visitor);
        var context = context("synthetic-party", "synthetic-wallet");
        context.unsetWithdrawal();

        assertThrows(TException.class, () -> handler.inspectWithdrawal(context));
        verifyNoInteractions(visitor, payoutResults);
    }

    private FraudInspectorHandler handler(TemplateVisitor<PayoutModel, CheckedResultModel> payoutVisitor) {
        return new FraudInspectorHandler("unused", new CheckedResultToRiskScoreConverter(), null, null, null, null,
                new WithdrawalContextToModelConverter(), payoutVisitor, "payout-result", payoutResults);
    }

    private static WithdrawalContext context(String party, String wallet) {
        return new WithdrawalContext()
                .setParty(new Party(new PartyConfigRef(party)))
                .setWalletRef(new WalletConfigRef(wallet))
                .setWithdrawalId("synthetic-withdrawal")
                .setWithdrawal(new Withdrawal()
                        .setBody(new Cash(100, new CurrencyRef("RUB")))
                        .setCreatedAt("2026-01-01T12:00:00Z")
                        .setDestination(Destination.digital_wallet(new DigitalWallet()
                                .setId("synthetic-destination"))));
    }
}
