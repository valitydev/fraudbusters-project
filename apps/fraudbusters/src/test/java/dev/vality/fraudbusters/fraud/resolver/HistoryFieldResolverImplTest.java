package dev.vality.fraudbusters.fraud.resolver;

import dev.vality.fraudbusters.config.payout.PayoutFraudoConfig;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.constant.PayoutCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.payment.resolver.PaymentModelFieldResolver;
import dev.vality.fraudbusters.util.BeanUtil;
import dev.vality.fraudo.exception.UnresolvableFieldException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class HistoryFieldResolverImplTest {

    private final HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPaymentHistory =
            BeanUtil.createPayoutFieldsInPaymentHistory();
    private final HistoryFieldResolverImpl<PayoutModel, PayoutCheckedField> payoutFieldsInPayoutHistory =
            BeanUtil.createPayoutFieldsInPayoutHistory();
    private final HistoryFieldResolverImpl<PaymentModel, PaymentCheckedField> paymentFieldsInPayoutHistory =
            new PayoutFraudoConfig().paymentFieldsInPayoutHistory(new PaymentModelFieldResolver());

    @Test
    void takesTheColumnFromTheTableAndTheValueFromTheOperation() {
        var payout = new PayoutModel();
        payout.setBinCountryCode("RUS");
        var payment = new PaymentModel();
        payment.setBinCountryCode("KAZ");

        var inPayments = payoutFieldsInPaymentHistory.resolve(PayoutCheckedField.COUNTRY_BANK, payout);
        assertEquals("bankCountry", inPayments.getName());
        assertEquals("RUS", inPayments.getValue());

        var inPayouts = payoutFieldsInPayoutHistory.resolve(PayoutCheckedField.COUNTRY_BANK, payout);
        assertEquals("issuerCountry", inPayouts.getName());
        assertEquals("RUS", inPayouts.getValue());

        var paymentInPayouts = paymentFieldsInPayoutHistory.resolve(PaymentCheckedField.COUNTRY_BANK, payment);
        assertEquals("issuerCountry", paymentInPayouts.getName());
        assertEquals("KAZ", paymentInPayouts.getValue());
    }

    @Test
    void rejectsFieldsTheTableDoesNotStore() {
        assertFalse(payoutFieldsInPaymentHistory.isSupported(PayoutCheckedField.WALLET_ID));
        assertTrue(payoutFieldsInPayoutHistory.isSupported(PayoutCheckedField.WALLET_ID));
        assertFalse(payoutFieldsInPayoutHistory.isSupported(PayoutCheckedField.MASKED_PAN));
        assertFalse(paymentFieldsInPayoutHistory.isSupported(PaymentCheckedField.SHOP_ID));
        assertThrows(UnresolvableFieldException.class,
                () -> payoutFieldsInPaymentHistory.resolve(PayoutCheckedField.WALLET_ID, new PayoutModel()));
        assertThrows(UnresolvableFieldException.class,
                () -> paymentFieldsInPayoutHistory.resolve(PaymentCheckedField.SHOP_ID));
    }

}
