package dev.vality.fraudo;

import dev.vality.fraudo.FraudoPaymentParser.ParseContext;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.exception.UnknownResultException;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.test.model.PaymentModel;
import dev.vality.fraudo.utils.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CustomTest extends AbstractPaymentTest {

    public static final String TEST_GMAIL_RU = "test@gmail.ru";

    @Test
    void trustTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/trust.frd");
        ResultModel result = parseAndVisit(resourceAsStream);
        assertEquals(ResultStatus.TRUST, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void threeDsTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/three_ds.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        ResultModel result = parseAndVisit(resourceAsStream);
        assertEquals(ResultStatus.THREE_DS, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void highRiskTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/highRisk.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        ResultModel result = parseAndVisit(resourceAsStream);
        assertEquals(ResultStatus.HIGH_RISK, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void notifyTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/notify.frd");
        ResultModel result = parseAndVisit(resourceAsStream);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
        assertEquals(1, ResultUtils.getNotifications(result).size());
    }

    @Test
    void declineTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/decline.frd");
        ResultModel result = parseAndVisit(resourceAsStream);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("test_11", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());
    }

    @Test
    void declineAndNotifyTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/declineAndNotify.frd");
        ResultModel result = parseAndVisit(resourceAsStream);
        ResultStatus resultStatus = ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus();
        assertEquals(ResultStatus.DECLINE_AND_NOTIFY, resultStatus);
        assertEquals("test_11", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());
        assertEquals(1, ResultUtils.getNotifications(result).size());
    }

    @Test
    void acceptTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/accept.frd");
        ResultModel result = parseAndVisit(resourceAsStream);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void ruleIsNotFireTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/rule_is_not_fire.frd");
        ResultModel result = parseAndVisit(resourceAsStream);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
    }

    @Test
    void notImplOperatorTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/unknownResult.frd");
        assertThrows(UnknownResultException.class, () -> parseAndVisit(resourceAsStream));
    }

    @Test
    void inTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/in.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setEmail(TEST_GMAIL_RU);
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void inCountryTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/in_country.frd");
        when(countryResolver.resolveCountry(any(), anyString())).thenReturn("RU");

        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setIp("127.0.0.1");
        model.setAmount(500L);
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void inCurrencyTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/in_currency.frd");

        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setCurrency("EUR");
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void tokenProviderTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/is_mobile.frd");
        when(paymentModelPaymentTypeResolver.isMobile(any())).thenReturn(true);

        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void payerTypeTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/is_recurrent.frd");
        when(paymentModelPaymentTypeResolver.isRecurrent(any())).thenReturn(true);

        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void amountTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/amount.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setAmount(56L);
        model.setCurrency("RUB");
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());

        model.setCurrency("USD");
        result = invoke(parseContext, model);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
    }

    @Test
    void paymentSystemTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/paymentSystem.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setPaymentSystem("VISA");
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());

        model.setPaymentSystem("MIR");
        result = invoke(parseContext, model);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
    }

    @Test
    void randTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/rand.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invoke(parseContext, new PaymentModel());
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void cardCategoryTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/cardCategory.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setCardCategory("credit");
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());

        model.setCardCategory("debit");
        result = invoke(parseContext, model);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
    }

    @Test
    void catchTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/catch.frd");
        when(uniqueValueAggregator.countUniqueValue(any(), any(), any(), any(), any()))
                .thenThrow(new UnknownResultException("as"));
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void likeTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/like.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setEmail(TEST_GMAIL_RU);
        model.setBin("553619");
        model.setPan("9137");
        ResultModel result = invoke(parseContext, model);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());

        model.setPan("9111");
        result = invoke(parseContext, model);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
    }

    @Test
    void inNotTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/in_not.frd");
        ParseContext parseContext = getParseContext(resourceAsStream);
        PaymentModel model = new PaymentModel();
        model.setEmail(TEST_GMAIL_RU);
        ResultModel result = invoke(parseContext, model);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
    }

    @Test
    void uniqCountTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/count_uniq.frd");
        when(uniqueValueAggregator.countUniqueValue(any(), any(), any(), any(), any())).thenReturn(2);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void uniqCountGroupByTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/count_uniqGroupBy_window.frd");
        when(uniqueValueAggregator.countUniqueValue(any(), any(), any(), any(), any())).thenReturn(2);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);

        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        verify(uniqueValueAggregator, times(1)).countUniqueValue(any(), any(), any(), any(), any());
    }

    @Test
    void eqCountryTest() throws Exception {
        InputStream resourceAsStream = CustomTest.class.getResourceAsStream("/rules/eq_country.frd");

        when(countryResolver.resolveCountry(any(), anyString())).thenReturn("RU");

        ResultModel result = parseAndVisit(resourceAsStream);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
        assertEquals(1, ResultUtils.getNotifications(result).size());

        when(countryResolver.resolveCountry(any(), anyString())).thenReturn("US");
        resourceAsStream = CustomTest.class.getResourceAsStream("/rules/eq_country.frd");
        result = parseAndVisit(resourceAsStream);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());

        resourceAsStream = CustomTest.class.getResourceAsStream("/rules/accept_with_notify.frd");
        result = parseAndVisit(resourceAsStream);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals(2, ResultUtils.getNotifications(result).size());
    }
}
