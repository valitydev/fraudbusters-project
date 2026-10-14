package dev.vality.fraudbusters.mg.connector.utils;

import dev.vality.damsel.base.Content;
import dev.vality.damsel.domain.*;
import dev.vality.damsel.payment_processing.InvoicePayment;
import dev.vality.damsel.payment_processing.InvoicePaymentChargeback;
import dev.vality.damsel.payment_processing.InvoiceRefundSession;
import dev.vality.fistful.base.CardType;
import dev.vality.fistful.base.Residence;
import dev.vality.geck.common.util.TypeUtil;
import dev.vality.geck.serializer.kit.mock.MockMode;
import dev.vality.geck.serializer.kit.mock.MockTBaseProcessor;
import dev.vality.geck.serializer.kit.tbase.TBaseHandler;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static dev.vality.fraudbusters.mg.connector.utils.MgEventSinkFlowGenerator.createCash;

public class BuildUtils {

    public static dev.vality.fistful.base.BankCard buildFistfulBankCard() {
        return new dev.vality.fistful.base.BankCard()
                .setBankName(InvoiceTestConstant.BANK_NAME)
                .setBin(InvoiceTestConstant.CARD_BIN)
                .setCategory(InvoiceTestConstant.CARD_CATEGORY)
                .setIssuerCountry(Residence.PAN)
                .setPaymentSystem(new dev.vality.fistful.base.PaymentSystemRef("mastercard"))
                .setToken(InvoiceTestConstant.CARD_TOKEN_PROVIDER)
                .setMaskedPan(InvoiceTestConstant.CARD_MASKED_PAN)
                .setCardType(CardType.debit)
                .setCardholderName(InvoiceTestConstant.CARDHOLDER_NAME);
    }

    public static dev.vality.fistful.base.CryptoWallet buildFistfulCryptoWallet() {
        dev.vality.fistful.base.CryptoWallet cryptoWallet = new dev.vality.fistful.base.CryptoWallet();
        cryptoWallet.setId("id");
        cryptoWallet.setCurrency(new dev.vality.fistful.base.CryptoCurrencyRef("bitcoin"));
        return cryptoWallet;
    }

    public static dev.vality.fistful.base.DigitalWallet buildFistfulDigitalWallet() {
        dev.vality.fistful.base.DigitalWallet digitalWallet = new dev.vality.fistful.base.DigitalWallet();
        digitalWallet.setId("id");
        digitalWallet.setPaymentService(new dev.vality.fistful.base.PaymentServiceRef("webmoney"));
        return digitalWallet;
    }

    public static dev.vality.damsel.payment_processing.Invoice buildInvoice(
            int sequenceId,
            String partyId,
            String shopId,
            String invoiceId,
            String paymentId,
            String refundId,
            String chargebackId,
            InvoiceStatus invoiceStatus,
            InvoicePaymentStatus paymentStatus) throws IOException {
        MockTBaseProcessor thriftBaseProcessor = new MockTBaseProcessor(MockMode.RANDOM, 15, 1);
        dev.vality.damsel.payment_processing.Invoice invoice = new dev.vality.damsel.payment_processing.Invoice()
                .setInvoice(buildInvoice(partyId, shopId, invoiceId, invoiceStatus, thriftBaseProcessor))
                .setPayments(buildPayments(partyId, shopId, paymentId, refundId, chargebackId, paymentStatus,
                        thriftBaseProcessor))
                .setLatestEventId(sequenceId);

        if (invoice.getPayments().get(0).getPayment().getPayer().isSetPaymentResource()) {
            invoice.getPayments().get(0).getPayment().getPayer().getPaymentResource().getResource()
                    .setPaymentTool(PaymentTool
                            .bank_card(thriftBaseProcessor.process(
                                    new BankCard(),
                                    new TBaseHandler<>(BankCard.class))));
        }

        return invoice;
    }

    private static Invoice buildInvoice(
            String partyId,
            String shopId,
            String invoiceId,
            InvoiceStatus invoiceStatus,
            MockTBaseProcessor thriftBaseProcessor) throws IOException {
        return thriftBaseProcessor.process(
                new Invoice(),
                new TBaseHandler<>(Invoice.class))
                .setId(invoiceId)
                .setShopRef(new ShopConfigRef(shopId))
                .setPartyRef(new PartyConfigRef(partyId))
                .setCreatedAt(TypeUtil.temporalToString(Instant.now()))
                .setContext(new Content("lel", ByteBuffer.wrap("{\"payment_id\": 271771960}".getBytes())))
                .setDue("2016-03-22T06:12:27Z")
                .setStatus(invoiceStatus);
    }

    private static List<InvoicePayment> buildPayments(
            String partyId,
            String shopId,
            String paymentId,
            String refundId,
            String chargebackId,
            InvoicePaymentStatus paymentStatus,
            MockTBaseProcessor thriftBaseProcessor) throws IOException {
        return Collections.singletonList(
                new InvoicePayment()
                        .setPayment(buildPayment(partyId, shopId, paymentId, paymentStatus, thriftBaseProcessor))
                        .setRefunds(buildRefunds(refundId, thriftBaseProcessor))
                        .setChargebacks(List.of(buildChargeback(chargebackId, thriftBaseProcessor)))
                        .setCashFlow(createCashFlow(1000L, 300L))
                        .setSessions(Collections.emptyList()));
    }

    public static List<FinalCashFlowPosting> createCashFlow(long l, long l2) {
        return List.of(
                payment(l),
                systemFee(100L),
                providerFee(20L),
                externalFee(10L),
                guaranteeDeposit(l2),
                incorrectPosting(99_999L));
    }

    public static List<FinalCashFlowPosting> createReversedCashFlow(long l, long l2) {
        return List.of(
                reversedPayment(l),
                reversedSystemFee(100L),
                reversedProviderFee(20L),
                reversedExternalFee(10L),
                reversedGuaranteeDeposit(l2),
                incorrectPosting(99_999L));
    }

    private static FinalCashFlowPosting payment(long amount) {
        return new FinalCashFlowPosting()
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.provider(
                                                        ProviderCashFlowAccount.settlement))))
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.settlement))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting systemFee(long amount) {
        return new FinalCashFlowPosting()
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.settlement))))
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.system(
                                                        SystemCashFlowAccount.settlement))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting providerFee(long amount) {
        return new FinalCashFlowPosting()
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.system(
                                                        SystemCashFlowAccount.settlement))))
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.provider(
                                                        ProviderCashFlowAccount.settlement))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting externalFee(long amount) {
        return new FinalCashFlowPosting()
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.system(
                                                        SystemCashFlowAccount.settlement))))
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.external(
                                                        ExternalCashFlowAccount.outcome))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting guaranteeDeposit(long amount) {
        return new FinalCashFlowPosting()
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.settlement))))
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.guarantee))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting reversedPayment(long amount) {
        return new FinalCashFlowPosting()
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.provider(
                                                        ProviderCashFlowAccount.settlement))))
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.settlement))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting reversedSystemFee(long amount) {
        return new FinalCashFlowPosting()
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.settlement))))
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.system(
                                                        SystemCashFlowAccount.settlement))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting reversedProviderFee(long amount) {
        return new FinalCashFlowPosting()
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.system(
                                                        SystemCashFlowAccount.settlement))))
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.provider(
                                                        ProviderCashFlowAccount.settlement))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting reversedExternalFee(long amount) {
        return new FinalCashFlowPosting()
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.system(
                                                        SystemCashFlowAccount.settlement))))
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.external(
                                                        ExternalCashFlowAccount.outcome))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting reversedGuaranteeDeposit(long amount) {
        return new FinalCashFlowPosting()
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.settlement))))
                .setSource(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.guarantee))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static FinalCashFlowPosting incorrectPosting(long amount) {
        return new FinalCashFlowPosting()
                .setDestination(
                        new FinalCashFlowAccount()
                                .setAccountId(1L)
                                .setAccountType(
                                        new CashFlowAccount(
                                                CashFlowAccount.merchant(
                                                        MerchantCashFlowAccount.settlement))))
                .setVolume(new Cash()
                        .setAmount(amount));
    }

    private static dev.vality.damsel.domain.InvoicePayment buildPayment(
            String partyId,
            String shopId,
            String paymentId,
            InvoicePaymentStatus paymentStatus,
            MockTBaseProcessor thriftBaseProcessor) throws IOException {
        dev.vality.damsel.domain.InvoicePayment process = thriftBaseProcessor.process(
                new dev.vality.damsel.domain.InvoicePayment(),
                new TBaseHandler<>(dev.vality.damsel.domain.InvoicePayment.class));
        return process
                .setCreatedAt("2016-03-22T06:12:27Z")
                .setId(paymentId)
                .setPartyRef(new PartyConfigRef(partyId))
                .setShopRef(new ShopConfigRef(shopId))
                .setCost(new Cash()
                        .setAmount(123L)
                        .setCurrency(new CurrencyRef("RUB")))
                .setStatus(paymentStatus);
    }

    private static List<dev.vality.damsel.payment_processing.InvoicePaymentRefund> buildRefunds(
            String refundId,
            MockTBaseProcessor thriftBaseProcessor) throws IOException {
        dev.vality.damsel.payment_processing.InvoicePaymentRefund invoicePaymentRefund =
                new dev.vality.damsel.payment_processing.InvoicePaymentRefund(
                        buildRefund(refundId, thriftBaseProcessor),
                        Collections.singletonList(new InvoiceRefundSession().setTransactionInfo(getTransactionInfo())));
        invoicePaymentRefund.setCashFlow(createCashFlow(123L, 100L));

        return Collections.singletonList(invoicePaymentRefund);
    }

    private static InvoicePaymentRefund buildRefund(String refundId, MockTBaseProcessor thriftBaseProcessor)
            throws IOException {
        return thriftBaseProcessor.process(
                new InvoicePaymentRefund(),
                new TBaseHandler<>(InvoicePaymentRefund.class))
                .setReason("keksik")
                .setCreatedAt(TypeUtil.temporalToString(Instant.now()))
                .setId(refundId);
    }

    private static InvoicePaymentChargeback buildChargeback(String chargebackId, MockTBaseProcessor thriftBaseProcessor)
            throws IOException {
        return thriftBaseProcessor.process(
                new InvoicePaymentChargeback(),
                new TBaseHandler<>(InvoicePaymentChargeback.class))
                .setChargeback(new dev.vality.damsel.domain.InvoicePaymentChargeback()
                        .setCreatedAt(TypeUtil.temporalToString(Instant.now()))
                        .setId(chargebackId)
                        .setReason(new InvoicePaymentChargebackReason()
                                .setCategory(InvoicePaymentChargebackCategory
                                        .fraud(new InvoicePaymentChargebackCategoryFraud())))
                        .setBody(createCash())
                        .setLevy(createCash())
                        .setStage(
                                InvoicePaymentChargebackStage.chargeback(new InvoicePaymentChargebackStageChargeback()))
                        .setStatus(InvoicePaymentChargebackStatus.pending(new InvoicePaymentChargebackPending()))
                ).setCashFlow(createCashFlow(23L, 100L));
    }

    private static TransactionInfo getTransactionInfo() {
        return new TransactionInfo()
                .setId(UUID.randomUUID().toString())
                .setExtra(Map.of())
                .setAdditionalInfo(getAdditionalInfo());
    }

    private static AdditionalTransactionInfo getAdditionalInfo() {
        return new AdditionalTransactionInfo()
                .setRrn("chicken-teriyaki");
    }
}
