package dev.vality.fraudo;

import dev.vality.fraudo.FraudoPaymentParser.ParseContext;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.test.model.PaymentModel;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import java.io.InputStream;
import java.util.concurrent.CountDownLatch;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RealTimerTest extends AbstractPaymentTest {

    public static final long TIME_CALL_AGGR_FUNC = 200L;
    public static final long MILLISTIME_FAST_FUNC = 10L;
    public static final long TIME_CALLING = 300L;

    @Test
    void timingTest() throws Exception {
        InputStream resourceAsStream = RealTimerTest.class.getResourceAsStream("/rules/payment_template.frd");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mockAggr(countDownLatch);

        ParseContext parseContext = getParseContext(resourceAsStream);

        PaymentModel model = new PaymentModel();
        model.setAmount(MILLISTIME_FAST_FUNC);
        model.setBin("444443");
        model.setBinCountryCode("RUS");

        long start = System.currentTimeMillis();
        ResultModel result = invoke(parseContext, model);
        long executionTime = System.currentTimeMillis() - start;

        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertEquals(0, countDownLatch.getCount());
        assertTrue(executionTime < TIME_CALL_AGGR_FUNC + 1 + TIME_CALLING);

        System.out.println("timingTest.executionTime=" + executionTime);

        result = invokeFullVisitor(parseContext, model);
        assertEquals(2, result.getRuleResults().size());
    }

    @Test
    void timingWithSuccessTest() throws Exception {
        InputStream resourceAsStream = RealTimerTest.class.getResourceAsStream("/rules/sum_and_count_template.frd");
        CountDownLatch countDownLatch = new CountDownLatch(1);
        mockAggr(countDownLatch);

        ParseContext parseContext = getParseContext(resourceAsStream);

        PaymentModel model = new PaymentModel();
        model.setAmount(MILLISTIME_FAST_FUNC);
        model.setBin("444443");
        model.setBinCountryCode("RUS");

        long start = System.currentTimeMillis();
        ResultModel result = invoke(parseContext, model);
        long executionTime = System.currentTimeMillis() - start;
        System.out.println("timingWithSuccessTest.executionTime=" + executionTime);

        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        assertTrue(executionTime < TIME_CALL_AGGR_FUNC * 4 + TIME_CALLING);
    }

    private void mockAggr(CountDownLatch countDownLatch) {
        when(countPaymentAggregator.count(any(), any(), any(), any()))
                .thenAnswer((Answer<Integer>) invocationOnMock -> {
                    Thread.sleep(TIME_CALL_AGGR_FUNC);
                    countDownLatch.countDown();
                    return 1;
                });
        when(inListFinder.findInWhiteList(anyList(), any()))
                .thenAnswer((Answer<Boolean>) invocationOnMock -> {
                    Thread.sleep(MILLISTIME_FAST_FUNC);
                    return false;
                });
        when(inListFinder.findInGreyList(anyList(), any()))
                .thenAnswer((Answer<Boolean>) invocationOnMock -> {
                    Thread.sleep(MILLISTIME_FAST_FUNC);
                    return false;
                });
        when(inListFinder.findInBlackList(anyList(), any()))
                .thenAnswer((Answer<Boolean>) invocationOnMock -> {
                    Thread.sleep(MILLISTIME_FAST_FUNC);
                    return false;
                });

        when(countryResolver.resolveCountry(any(), anyString()))
                .thenAnswer((Answer<String>) invocationOnMock -> "RUS");
    }
}
