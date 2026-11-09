package dev.vality.fraudo.payout.validator;

import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.FraudoRulesParser;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static dev.vality.fraudo.FraudoRulesParser.*;
import static dev.vality.fraudo.payout.validator.HistoryFunctions.PAYMENT_HISTORY_FUNCTIONS;
import static dev.vality.fraudo.payout.validator.HistoryFunctions.PAYOUT_HISTORY_FUNCTIONS;
import static dev.vality.fraudo.utils.TextUtil.safeGetText;

/** Validates payout rules using the shared grammar, without changing payment rule validation. */
@RequiredArgsConstructor
public class PayoutTemplateValidator {

    private final PayoutRuleFields fields;

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
            errors.add("3ds is not supported for payouts");
        } else if (tree instanceof Is_trustedContext) {
            errors.add("isTrusted is not supported for payouts");
        } else if (tree instanceof Is_mobileContext || tree instanceof Is_recurrentContext) {
            errors.add("isMobile and isRecurrent are not supported for payouts");
        } else if (tree instanceof ParserRuleContext ctx) {
            validateFields(ctx, errors);
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            validateTree(tree.getChild(i), errors);
        }
    }

    private void validateFields(ParserRuleContext ctx, List<String> errors) {
        if (PAYMENT_HISTORY_FUNCTIONS.contains(ctx.getClass())) {
            checkFieldsSupported(ctx, collectAggregateFields(ctx), fields.paymentHistory(), errors);
        } else if (PAYOUT_HISTORY_FUNCTIONS.contains(ctx.getClass())) {
            checkFieldsSupported(ctx, collectAggregateFields(ctx), fields.payoutHistory(), errors);
        } else if (ctx instanceof InContext in && in.stringExpression().STRING() != null) {
            checkFieldsSupported(ctx, List.of(in.stringExpression().STRING()), fields.operation(), errors);
        } else if (ctx instanceof LikeContext like) {
            checkFieldsSupported(ctx, List.of(like.STRING(0)), fields.operation(), errors);
        } else if (ctx instanceof Country_byContext countryBy) {
            checkFieldsSupported(ctx, List.of(countryBy.STRING()), fields.country(), errors);
        } else if (ctx instanceof In_white_listContext list) {
            checkFieldsSupported(ctx, list.string_list().STRING(), fields.operation(), errors);
        } else if (ctx instanceof In_black_listContext list) {
            checkFieldsSupported(ctx, list.string_list().STRING(), fields.operation(), errors);
        } else if (ctx instanceof In_listContext list) {
            checkFieldsSupported(ctx, list.string_list().STRING(), fields.operation(), errors);
        } else if (ctx instanceof In_grey_listContext list) {
            checkFieldsSupported(ctx, list.string_list().STRING(), fields.payoutHistory(), errors);
        }
    }

    /** The counted field, the unique field for unique functions, and the grouping fields; not the error code. */
    private List<TerminalNode> collectAggregateFields(ParserRuleContext ctx) {
        boolean unique = ctx instanceof UniqueContext || ctx instanceof Unique_payoutsContext;
        List<TerminalNode> result = new ArrayList<>(ctx.getTokens(STRING).subList(0, unique ? 2 : 1));
        Group_byContext groupBy = ctx.getRuleContext(Group_byContext.class, 0);
        if (groupBy != null) {
            result.addAll(groupBy.string_list().STRING());
        }
        return result;
    }

    private void checkFieldsSupported(ParserRuleContext ctx, List<TerminalNode> nodes, Set<String> allowed,
                                      List<String> errors) {
        for (TerminalNode node : nodes) {
            String field = safeGetText(node);
            if (!allowed.contains(field)) {
                errors.add("field " + field + " is not supported in " + ctx.getStart().getText() + " for payouts");
            }
        }
    }

}
