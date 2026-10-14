package dev.vality.fraudo;

import dev.vality.fraudo.FraudoPaymentParser.ParseContext;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.model.TrustCondition;
import dev.vality.fraudo.test.model.PaymentModel;
import dev.vality.fraudo.utils.ResultUtils;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IsTrustedTest extends AbstractPaymentTest {

    @Test
    void trustedTest() {
        when(customerTypeResolver.isTrusted(any())).thenReturn(true);
        testIsTrusted("/rules/is_trusted.frd");
    }

    @Test
    void trustedWithTemplateNameTest() {
        when(customerTypeResolver.isTrusted(any())).thenReturn(true);
        testIsTrusted("/rules/is_trusted_with_template_name.frd");
    }

    @Test
    void trustedWithWithdrawalsConditionsTest() {
        when(customerTypeResolver.isTrusted(any(), isNull(List.class), anyList()))
                .thenReturn(true);
        testIsTrusted("/rules/is_trusted_with_withdrawals_conditions.frd");
    }

    @Test
    void trustedWithPaymentsConditionsTest() {
        when(customerTypeResolver.isTrusted(any(), anyList(), isNull(List.class)))
                .thenReturn(true);
        testIsTrusted("/rules/is_trusted_with_payments_conditions.frd");
    }

    @Test
    void trustedWithPaymentsAndWithdrawalSingleConditionsTest() {
        when(customerTypeResolver.isTrusted(any(), anyList(), anyList()))
                .thenReturn(true);
        testIsTrusted("/rules/is_trusted_with_payments_and_withdrawals_single_conditions.frd");
    }

    @Test
    void trustedWithPaymentsAndWithdrawalTest() {
        when(customerTypeResolver.isTrusted(any(), anyList(), anyList()))
                .thenReturn(true);
        testIsTrusted("/rules/is_trusted_with_payments_and_withdrawals_conditions.frd");

        ArgumentCaptor<PaymentModel> paymentModelCaptor = ArgumentCaptor.forClass(PaymentModel.class);
        ArgumentCaptor<List<TrustCondition>> paymentsCaptor = ArgumentCaptor.forClass((Class) List.class);
        ArgumentCaptor<List<TrustCondition>> withdrawalsCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(customerTypeResolver, times(1))
                .isTrusted(paymentModelCaptor.capture(), paymentsCaptor.capture(), withdrawalsCaptor.capture());

        assertEquals(1, paymentModelCaptor.getAllValues().size());
        assertNotNull(paymentModelCaptor.getValue());
        assertEquals(1, paymentsCaptor.getAllValues().size());
        List<TrustCondition> payments = paymentsCaptor.getValue();
        assertTrustedCondition("RUB", 1, 1000, 10, payments.get(0));
        assertTrustedCondition("EUR", 2, 20, null, payments.get(1));
        assertEquals(1, withdrawalsCaptor.getAllValues().size());
        List<TrustCondition> withdrawals = withdrawalsCaptor.getValue();
        assertTrustedCondition("USD", 3, 3000, 3, withdrawals.get(0));
        assertTrustedCondition("CAD", 4, 4, null, withdrawals.get(1));

    }

    @SneakyThrows
    private void testIsTrusted(String testCaseFilePath) {
        InputStream resourceAsStream = IsTrustedTest.class.getResourceAsStream(testCaseFilePath);
        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    private void assertTrustedCondition(
            String currency, Integer yearsOffset, Integer count, Integer sum, TrustCondition condition) {
        assertEquals(currency, condition.getTransactionsCurrency());
        assertEquals(yearsOffset, condition.getTransactionsYearsOffset());
        assertEquals(count, condition.getTransactionsCount());
        assertEquals(sum, condition.getTransactionsSum());
    }

}
