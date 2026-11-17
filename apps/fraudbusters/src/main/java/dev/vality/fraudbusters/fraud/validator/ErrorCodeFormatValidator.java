package dev.vality.fraudbusters.fraud.validator;

import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.FraudoRulesParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

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
        return findMisformattedErrorCodes(parser.parse());
    }

    private List<String> findMisformattedErrorCodes(ParseTree tree) {
        Stream<String> ownError = isErrorCodeFunction(tree)
                ? findMisformattedErrorCode((ParserRuleContext) tree).stream()
                : Stream.empty();
        Stream<String> childErrors = IntStream.range(0, tree.getChildCount())
                .mapToObj(tree::getChild)
                .flatMap(child -> findMisformattedErrorCodes(child).stream());
        return Stream.concat(ownError, childErrors)
                .collect(Collectors.toList());
    }

    private static boolean isErrorCodeFunction(ParseTree tree) {
        return tree instanceof Count_errorContext
                || tree instanceof Sum_errorContext
                || tree instanceof Count_payouts_errorContext
                || tree instanceof Sum_payouts_errorContext;
    }

    private Optional<String> findMisformattedErrorCode(ParserRuleContext function) {
        List<TerminalNode> arguments = function.getTokens(STRING);
        if (arguments.size() < 2) {
            return Optional.empty();
        }
        String errorCode = safeGetText(arguments.get(1));
        if (!MISPLACED_SPACE.matcher(errorCode).find()) {
            return Optional.empty();
        }
        return Optional.of(String.format(MISPLACED_SPACE_ERROR,
                function.getStart().getLine(), errorCode, function.getStart().getText()));
    }

}
