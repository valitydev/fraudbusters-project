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
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.vality.fraudo.FraudoRulesParser.*;
import static dev.vality.fraudo.utils.TextUtil.safeGetText;

public class PaymentHistoryFieldValidator {

    private static final Set<Class<? extends ParserRuleContext>> PAYMENT_HISTORY_FUNCTIONS = Set.of(
            CountContext.class, Count_successContext.class, Count_pendingContext.class, Count_errorContext.class,
            Count_chargebackContext.class, Count_refundContext.class, SumContext.class, Sum_successContext.class,
            Sum_errorContext.class, Sum_chargebackContext.class, Sum_refundContext.class, UniqueContext.class);
    private static final String MISSING_FIELD_ERROR =
            "line %d: field %s is not stored in payment history, %s cannot use it";

    private final Set<String> fieldsStoredInHistory;

    public PaymentHistoryFieldValidator(DatabasePaymentFieldResolver databasePaymentFieldResolver) {
        this.fieldsStoredInHistory = Arrays.stream(PaymentCheckedField.values())
                .filter(field -> isStoredInHistory(databasePaymentFieldResolver, field))
                .map(PaymentCheckedField::getValue)
                .collect(Collectors.toUnmodifiableSet());
    }

    public List<String> findFieldsMissingInHistory(String template) {
        FraudoRulesParser parser = new FraudoRulesParser(
                new CommonTokenStream(new FraudoRulesLexer(CharStreams.fromString(template))));
        parser.removeErrorListeners();
        return findFieldsMissingInHistory(parser.parse());
    }

    private List<String> findFieldsMissingInHistory(ParseTree tree) {
        Stream<String> ownErrors = tree instanceof ParserRuleContext function
                && PAYMENT_HISTORY_FUNCTIONS.contains(function.getClass())
                ? findFunctionFieldsMissingInHistory(function)
                : Stream.empty();
        Stream<String> childErrors = IntStream.range(0, tree.getChildCount())
                .mapToObj(tree::getChild)
                .flatMap(child -> findFieldsMissingInHistory(child).stream());
        return Stream.concat(ownErrors, childErrors)
                .collect(Collectors.toList());
    }

    private Stream<String> findFunctionFieldsMissingInHistory(ParserRuleContext function) {
        return collectAggregateFields(function).stream()
                .filter(node -> !fieldsStoredInHistory.contains(safeGetText(node)))
                .map(node -> String.format(MISSING_FIELD_ERROR,
                        node.getSymbol().getLine(), safeGetText(node), function.getStart().getText()));
    }

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
