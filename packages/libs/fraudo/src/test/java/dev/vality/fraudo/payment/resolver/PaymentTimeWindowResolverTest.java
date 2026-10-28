package dev.vality.fraudo.payment.resolver;

import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.FraudoRulesParser;
import dev.vality.fraudo.model.TimeWindow;
import dev.vality.fraudo.resolver.TimeWindowResolver;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

import static dev.vality.fraudo.constant.TimeUnit.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


class PaymentTimeWindowResolverTest {

    private final TimeWindowResolver<FraudoRulesParser.Time_windowContext> timeWindowResolver =
            new PaymentTimeWindowResolver();

    @Test
    void withoutEndTimeAndWithoutTimeUnit() throws Exception {
        FraudoRulesParser.Time_windowContext timeWindowContext =
                getTimeWindowContext("/rules/time_window/withoutEndTimeAndWithoutTimeUnit.frd");

        TimeWindow timeWindow = timeWindowResolver.resolve(timeWindowContext);

        assertNotNull(timeWindow);
        assertEquals(24, timeWindow.getStart());
        assertEquals(0, timeWindow.getEnd());
        assertEquals(HOURS, timeWindow.getTimeUnit());
    }

    @Test
    void withEndTimeAndWithoutTimeUnit() throws Exception {
        FraudoRulesParser.Time_windowContext timeWindowContext =
                getTimeWindowContext("/rules/time_window/withEndTimeAndWithoutTimeUnit.frd");

        TimeWindow timeWindow = timeWindowResolver.resolve(timeWindowContext);

        assertNotNull(timeWindow);
        assertEquals(24, timeWindow.getStart());
        assertEquals(2, timeWindow.getEnd());
        assertEquals(HOURS, timeWindow.getTimeUnit());
    }

    @Test
    void withoutEndTimeAndWithDayTimeUnitTest() throws Exception {
        FraudoRulesParser.Time_windowContext timeWindowContext =
                getTimeWindowContext("/rules/time_window/withoutEndTimeAndWithDayTimeUnit.frd");

        TimeWindow timeWindow = timeWindowResolver.resolve(timeWindowContext);

        assertNotNull(timeWindow);
        assertEquals(24, timeWindow.getStart());
        assertEquals(0, timeWindow.getEnd());
        assertEquals(DAYS, timeWindow.getTimeUnit());
    }

    @Test
    void withEndTimeAndHourTimeUnitTest() throws Exception {
        FraudoRulesParser.Time_windowContext timeWindowContext =
                getTimeWindowContext("/rules/time_window/withEndTimeAndHourTimeUnit.frd");

        TimeWindow timeWindow = timeWindowResolver.resolve(timeWindowContext);

        assertNotNull(timeWindow);
        assertEquals(24, timeWindow.getStart());
        assertEquals(1, timeWindow.getEnd());
        assertEquals(HOURS, timeWindow.getTimeUnit());
    }

    @Test
    void withCalMonthsTimeUnitTest() throws Exception {
        FraudoRulesParser.Time_windowContext timeWindowContext =
                getTimeWindowContext("/rules/time_window/withCalMonthsTimeUnit.frd");

        TimeWindow timeWindow = timeWindowResolver.resolve(timeWindowContext);

        assertNotNull(timeWindow);
        assertEquals(4, timeWindow.getStart());
        assertEquals(2, timeWindow.getEnd());
        assertEquals(CALENDAR_MONTHS, timeWindow.getTimeUnit());
    }

    @Test
    void withCalDaysTimeUnitTest() throws Exception {
        FraudoRulesParser.Time_windowContext timeWindowContext =
                getTimeWindowContext("/rules/time_window/withCalDaysTimeUnit.frd");

        TimeWindow timeWindow = timeWindowResolver.resolve(timeWindowContext);

        assertNotNull(timeWindow);
        assertEquals(5, timeWindow.getStart());
        assertEquals(2, timeWindow.getEnd());
        assertEquals(CALENDAR_DAYS, timeWindow.getTimeUnit());
    }

    private FraudoRulesParser.Time_windowContext getTimeWindowContext(String path) throws IOException {
        InputStream resourceAsStream = PaymentTimeWindowResolverTest.class.getResourceAsStream(path);
        FraudoRulesLexer lexer = new FraudoRulesLexer(CharStreams.fromStream(resourceAsStream));
        FraudoRulesParser parser =
                new FraudoRulesParser(new CommonTokenStream(lexer));
        FraudoRulesParser.Time_windowContext timeWindowContext = parser.time_window();
        return timeWindowContext;
    }
}