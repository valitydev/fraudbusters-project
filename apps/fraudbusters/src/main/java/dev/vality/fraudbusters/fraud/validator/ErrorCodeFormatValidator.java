package dev.vality.fraudbusters.fraud.validator;

import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.FraudoRulesParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import static dev.vality.fraudo.FraudoRulesParser.*;
import static dev.vality.fraudo.utils.TextUtil.safeGetText;

public class ErrorCodeFormatValidator {

    private static final Pattern MISPLACED_SPACE = Pattern.compile("^\\s|\\s$|\\s:|:\\s");
    private static final String MISPLACED_SPACE_ERROR = """
            line %d: error code "%s" in %s has spaces at the edges or around ':'; \
            history stores codes without them, so the function never counts it""";

    public List<String> findMisformattedErrorCodes(String template) {
        FraudoRulesParser parser = new FraudoRulesParser(
                new CommonTokenStream(new FraudoRulesLexer(CharStreams.fromString(template))));
        parser.removeErrorListeners();
        List<String> errors = new ArrayList<>();
        collectMisformattedErrorCodes(parser.parse(), errors);
        return errors;
    }

    private void collectMisformattedErrorCodes(ParseTree tree, List<String> errors) {
        if (tree instanceof Count_errorContext
                || tree instanceof Sum_errorContext
                || tree instanceof Count_payouts_errorContext
                || tree instanceof Sum_payouts_errorContext) {
            checkErrorCode((ParserRuleContext) tree, errors);
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectMisformattedErrorCodes(tree.getChild(i), errors);
        }
    }

    private void checkErrorCode(ParserRuleContext function, List<String> errors) {
        List<TerminalNode> arguments = function.getTokens(STRING);
        if (arguments.size() < 2) {
            return;
        }
        String errorCode = safeGetText(arguments.get(1));
        if (MISPLACED_SPACE.matcher(errorCode).find()) {
            errors.add(String.format(MISPLACED_SPACE_ERROR,
                    function.getStart().getLine(), errorCode, function.getStart().getText()));
        }
    }

}
