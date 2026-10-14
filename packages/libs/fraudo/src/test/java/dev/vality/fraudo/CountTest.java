package dev.vality.fraudo;

import dev.vality.fraudo.FraudoPaymentParser.ParseContext;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CountTest extends AbstractPaymentTest {

    @Test
    void countTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/count.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        when(countPaymentAggregator.countError(any(), any(), any(), anyString(), any())).thenReturn(6);
        when(countPaymentAggregator.countSuccess(any(), any(), any(), any())).thenReturn(4);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("0", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());

        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(9);
        when(countPaymentAggregator.countError(any(), any(), any(), anyString(), any())).thenReturn(6);
        when(countPaymentAggregator.countSuccess(any(), any(), any(), any())).thenReturn(6);

        result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
    }

    @Test
    void countErrorTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/countError.frd");
        when(countPaymentAggregator.countError(any(), any(), any(), any())).thenReturn(6);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("0", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());
    }

    @Test
    void countPendingTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/countPending.frd");
        when(countPaymentAggregator.countPending(any(), any(), any(), any())).thenReturn(6);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("0", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());
    }

    @Test
    void countCargeRefundTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/count_chargeback_refund.frd");
        when(countPaymentAggregator.countChargeback(any(), any(), any(), any())).thenReturn(3);
        when(countPaymentAggregator.countRefund(any(), any(), any(), any())).thenReturn(5);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("0", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());
    }

    @Test
    void countGroupByTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/countGroupBy.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("0", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());

        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(1);
        result = invokeParse(parseContext);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());
    }

    @Test
    void countTimeWindowGroupByTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/countTimeWindowGroupBy.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("0", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());

        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(1);
        result = invokeParse(parseContext);
        assertFalse(ResultUtils.findFirstNotNotifyStatus(result).isPresent());

        resourceAsStream = CountTest.class.getResourceAsStream("/rules/countTimeWindowGroupBy_2.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        parseContext = getParseContext(resourceAsStream);
        result = invokeParse(parseContext);
        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals("0", ResultUtils.findFirstNotNotifyStatus(result).get().getRuleChecked());
    }

    @Test
    void countCardTokenTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/count_card_token.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);

        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        verify(countPaymentAggregator, times(1)).count(any(), any(), any(), any());
    }

    @Test
    void countPhoneTest() throws Exception {
        InputStream resourceAsStream = CountTest.class.getResourceAsStream("/rules/count_phone.frd");
        when(countPaymentAggregator.count(any(), any(), any(), any())).thenReturn(10);
        ParseContext parseContext = getParseContext(resourceAsStream);
        ResultModel result = invokeParse(parseContext);

        assertEquals(ResultStatus.DECLINE, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        verify(countPaymentAggregator, times(1)).count(any(), any(), any(), any());
    }

}
