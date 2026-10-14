package dev.vality.fraudo;

import dev.vality.fraudo.FraudoPaymentParser.ParseContext;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;

@ExtendWith(MockitoExtension.class)
public class SumTest extends AbstractPaymentTest {

    @Test
    void sumTest() throws Exception {
        InputStream resourceAsStream = SumTest.class.getResourceAsStream("/rules/sum.frd");
        Mockito.when(sumPaymentAggregator.sum(any(), any(), any(), any())).thenReturn(10500.60);
        Mockito.when(sumPaymentAggregator.sumError(any(), any(), any(), anyString(), any())).thenReturn(524.0);
        Mockito.when(sumPaymentAggregator.sumSuccess(any(), any(), any(), any())).thenReturn(4.0);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
        assertEquals(1, ResultUtils.getNotifications(result).size());

        Mockito.when(sumPaymentAggregator.sum(any(), any(), any(), any())).thenReturn(90.0);
        Mockito.when(sumPaymentAggregator.sumError(any(), any(), any(), anyString(), any())).thenReturn(504.0);
        Mockito.when(sumPaymentAggregator.sumSuccess(any(), any(), any(), any())).thenReturn(501.0);

        result = invokeParse(parseContext);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
        assertEquals(0, ResultUtils.getNotifications(result).size());
    }

    @Test
    void sumErrorTest() throws Exception {
        InputStream resourceAsStream = SumTest.class.getResourceAsStream("/rules/sumError.frd");
        Mockito.when(sumPaymentAggregator.sumError(any(), any(), any(), any())).thenReturn(524.0);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());

    }

    @Test
    void sumChargeRefundTest() throws Exception {
        InputStream resourceAsStream = SumTest.class.getResourceAsStream("/rules/sum_chargeback_refund.frd");
        Mockito.when(sumPaymentAggregator.sumChargeback(any(), any(), any(), any())).thenReturn(10000.60);
        Mockito.when(sumPaymentAggregator.sumRefund(any(), any(), any(), any())).thenReturn(10000.60);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void sumGroupByTest() throws Exception {
        InputStream resourceAsStream = SumTest.class.getResourceAsStream("/rules/sumGroupBy.frd");
        Mockito.when(sumPaymentAggregator.sum(any(), any(), any(), any())).thenReturn(10500.60);
        Mockito.when(sumPaymentAggregator.sumError(any(), any(), any(), anyString(), any())).thenReturn(524.0);
        Mockito.when(sumPaymentAggregator.sumSuccess(any(), any(), any(), any())).thenReturn(4.0);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);

        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
        assertEquals(1, ResultUtils.getNotifications(result).size());

        Mockito.when(sumPaymentAggregator.sum(any(), any(), any(), any())).thenReturn(90.0);
        Mockito.when(sumPaymentAggregator.sumError(any(), any(), any(), anyString(), any())).thenReturn(504.0);
        Mockito.when(sumPaymentAggregator.sumSuccess(any(), any(), any(), any())).thenReturn(501.0);

        result = invokeParse(parseContext);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
        assertEquals(0, ResultUtils.getNotifications(result).size());
    }

}
