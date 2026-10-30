package dev.vality.fraudo;

import dev.vality.fraudo.FraudoRulesParser.ParseContext;
import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.model.ResultModel;
import dev.vality.fraudo.test.model.PaymentModel;
import dev.vality.fraudo.utils.ResultUtils;
import lombok.SneakyThrows;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class IsTrustedTest extends AbstractPaymentTest {

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void trustedTest(boolean fullVisitor) {
        PaymentModel model = new PaymentModel();
        when(customerTypeResolver.isTrusted(model)).thenReturn(true);
        testIsTrusted("/rules/is_trusted.frd", model, fullVisitor);
        verify(customerTypeResolver).isTrusted(model);
        verifyNoMoreInteractions(customerTypeResolver);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void trustedWithTemplateNameTest(boolean fullVisitor) {
        PaymentModel model = new PaymentModel();
        when(customerTypeResolver.isTrusted(model, "trusted-template")).thenReturn(true);
        testIsTrusted("/rules/is_trusted_with_template_name.frd", model, fullVisitor);
        verify(customerTypeResolver).isTrusted(model, "trusted-template");
        verifyNoMoreInteractions(customerTypeResolver);
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "paymentsConditions(condition(\"RUB\", 1, 2))",
            "payoutsConditions(condition(\"RUB\", 1, 2))",
            "withdrawalsConditions(condition(\"RUB\", 1, 2))",
            "paymentsConditions(condition(\"RUB\", 1, 2)), payoutsConditions(condition(\"RUB\", 1, 2))"})
    void rejectsInlineConditions(String conditions) {
        var parser = new FraudoRulesParser(new CommonTokenStream(
                new FraudoRulesLexer(CharStreams.fromString("rule: isTrusted(" + conditions + ") -> accept;"))));
        parser.removeErrorListeners();
        parser.parse();
        assertTrue(parser.getNumberOfSyntaxErrors() > 0);
        verifyNoInteractions(customerTypeResolver);
    }

    @SneakyThrows
    private void testIsTrusted(String testCaseFilePath, PaymentModel model, boolean fullVisitor) {
        try (InputStream resourceAsStream = IsTrustedTest.class.getResourceAsStream(testCaseFilePath)) {
            ParseContext parseContext = getParseContext(resourceAsStream);
            ResultModel result = fullVisitor ? invokeFullVisitor(parseContext, model) : invoke(parseContext, model);
            assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(result).get().getResultStatus());
        }
    }

}
