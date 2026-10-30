package dev.vality.fraudbusters.fraud;

import dev.vality.fraudbusters.fraud.payment.PaymentContextParserImpl;
import dev.vality.fraudo.FraudoRulesParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class PaymentContextParserImplTest {

    private FraudContextParser<FraudoRulesParser.ParseContext> fraudContextParser = new PaymentContextParserImpl();

    @Test
    public void parse() {
        FraudoRulesParser.ParseContext parse = fraudContextParser.parse("rule: 3 > 2 AND 1 = 1 -> accept;");
        assertFalse(parse.fraud_rule().isEmpty());
    }
}
