package dev.vality.fraudo.payout.validator;

import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.FraudoRulesParser;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;

import java.util.ArrayList;
import java.util.List;

import static dev.vality.fraudo.FraudoRulesParser.*;

/** Validates withdrawal rules using the shared grammar, without changing payment rule validation. */
public class WithdrawalTemplateValidator {

    public List<String> validate(String template) {
        List<String> errors = new ArrayList<>();
        BaseErrorListener errorListener = new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                errors.add("line " + line + ":" + charPositionInLine + " " + msg);
            }
        };
        FraudoRulesLexer lexer = new FraudoRulesLexer(CharStreams.fromString(template));
        lexer.removeErrorListeners();
        lexer.addErrorListener(errorListener);
        FraudoRulesParser parser = new FraudoRulesParser(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener(errorListener);
        ParseContext tree = parser.parse();
        if (errors.isEmpty()) {
            validateTree(tree, errors);
        }
        return errors;
    }

    private void validateTree(ParseTree tree, List<String> errors) {
        if ((tree instanceof ResultContext || tree instanceof Catch_resultContext)
                && "3ds".equals(tree.getText())) {
            errors.add("3ds is not supported for withdrawals");
        } else if (tree instanceof Is_trustedContext) {
            errors.add("isTrusted is not supported for withdrawals");
        } else if (tree instanceof Is_mobileContext || tree instanceof Is_recurrentContext) {
            errors.add("isMobile and isRecurrent are not supported for withdrawals");
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            validateTree(tree.getChild(i), errors);
        }
    }

}
