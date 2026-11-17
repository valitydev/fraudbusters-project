package dev.vality.fraudbusters.fraud.payment.validator;

import dev.vality.fraudbusters.exception.UnknownFieldException;
import dev.vality.fraudbusters.fraud.constant.PaymentCheckedField;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudbusters.fraud.payment.resolver.DatabasePaymentFieldResolver;
import dev.vality.fraudo.FraudoRulesLexer;
import dev.vality.fraudo.FraudoRulesParser;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static dev.vality.fraudo.FraudoRulesParser.*;
import static dev.vality.fraudo.utils.TextUtil.safeGetText;

/**
 * Finds payment history functions over fields the payment history does not store, for example
 * {@code count("masked_pan", ...)}. Such a function fails on every inspection and its rule silently becomes a
 * notification, so a template with it is rejected when saved.
 */
public class PaymentHistoryFieldValidator {

    private static final Set<Class<? extends ParserRuleContext>> PAYMENT_HISTORY_FUNCTIONS = Set.of(
            CountContext.class, Count_successContext.class, Count_pendingContext.class, Count_errorContext.class,
            Count_chargebackContext.class, Count_refundContext.class, SumContext.class, Sum_successContext.class,
            Sum_errorContext.class, Sum_chargebackContext.class, Sum_refundContext.class, UniqueContext.class);

    private final Set<String> fieldsStoredInHistory;

    public PaymentHistoryFieldValidator(DatabasePaymentFieldResolver databasePaymentFieldResolver) {
        // The aggregators resolve fields with the same method, so the accepted set cannot drift from them.
        this.fieldsStoredInHistory = Arrays.stream(PaymentCheckedField.values())
                .filter(field -> isStoredInHistory(databasePaymentFieldResolver, field))
                .map(PaymentCheckedField::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    public List<String> findFieldsMissingInHistory(String template) {
        FraudoRulesParser parser = new FraudoRulesParser(
                new CommonTokenStream(new FraudoRulesLexer(CharStreams.fromString(template))));
        parser.removeErrorListeners();
        List<String> errors = new ArrayList<>();
        collectFieldsMissingInHistory(parser.parse(), errors);
        return errors;
    }

    private void collectFieldsMissingInHistory(ParseTree tree, List<String> errors) {
        if (tree instanceof ParserRuleContext function && PAYMENT_HISTORY_FUNCTIONS.contains(function.getClass())) {
            for (TerminalNode node : collectAggregateFields(function)) {
                String field = safeGetText(node);
                if (!fieldsStoredInHistory.contains(field)) {
                    errors.add(String.format("line %d: field %s is not stored in payment history, %s cannot use it",
                            node.getSymbol().getLine(), field, function.getStart().getText()));
                }
            }
        }
        for (int i = 0; i < tree.getChildCount(); i++) {
            collectFieldsMissingInHistory(tree.getChild(i), errors);
        }
    }

    /** The counted field, the unique field of unique, and the grouping fields; not the error code. */
    private static List<TerminalNode> collectAggregateFields(ParserRuleContext function) {
        List<TerminalNode> arguments = function.getTokens(STRING);
        int fieldCount = function instanceof UniqueContext ? 2 : 1;
        List<TerminalNode> fields = new ArrayList<>(arguments.subList(0, Math.min(fieldCount, arguments.size())));
        Group_byContext groupBy = function.getRuleContext(Group_byContext.class, 0);
        if (groupBy != null) {
            fields.addAll(groupBy.string_list().STRING());
        }
        return fields;
    }

    private static boolean isStoredInHistory(DatabasePaymentFieldResolver resolver, PaymentCheckedField field) {
        try {
            resolver.resolve(field, new PaymentModel());
            return true;
        } catch (UnknownFieldException e) {
            return false;
        }
    }

}
