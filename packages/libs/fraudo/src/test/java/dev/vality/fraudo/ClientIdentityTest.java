package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.test.model.PaymentModel;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ClientIdentityTest extends AbstractPaymentTest {

    @Test
    void userIdWorksInBothVisitorsAndPreservesCase() throws Exception {
        var parse = getParseContext(new ByteArrayInputStream((
                "rule: in(userId(), \"Synthetic-User\") -> accept;")
                .getBytes(StandardCharsets.UTF_8)));
        PaymentModel model = new PaymentModel();
        model.setCustomerId("synthetic-customer");
        model.setUserId("Synthetic-User");
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(invoke(parse, model))
                .orElseThrow().getResultStatus());
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(invokeFullVisitor(parse, model))
                .orElseThrow().getResultStatus());
        model.setUserId("synthetic-user");
        assertTrue(ResultUtils.findFirstNotNotifyStatus(invoke(parse, model)).isEmpty());
    }

    @Test
    void oldPaymentsWithoutIdentityRemainSupported() throws Exception {
        var parse = getParseContext(new ByteArrayInputStream((
                "rule: userId() = \"UNKNOWN\" -> accept;")
                .getBytes(StandardCharsets.UTF_8)));
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(invoke(parse, new PaymentModel()))
                .orElseThrow().getResultStatus());
    }

    @Test
    void customerIdIsNotALanguageFunction() {
        var lexer = new FraudoRulesLexer(org.antlr.v4.runtime.CharStreams.fromString(
                "rule: customerId() = \"synthetic-customer\" -> accept;"));
        var parser = new FraudoRulesParser(new org.antlr.v4.runtime.CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.parse();
        assertTrue(parser.getNumberOfSyntaxErrors() > 0);
    }
}
