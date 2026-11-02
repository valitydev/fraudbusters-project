package dev.vality.fraudo.utils.key.generator;

import dev.vality.fraudo.resolver.impl.TimeWindowResolverImpl;
import dev.vality.fraudo.utils.TextUtil;
import dev.vality.fraudo.utils.key.HistoryCacheKey;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.antlr.v4.runtime.tree.TerminalNode;

import java.util.List;
import java.util.function.Function;

import static dev.vality.fraudo.FraudoRulesParser.Group_byContext;
import static dev.vality.fraudo.FraudoRulesParser.Time_windowContext;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryKeyGenerator {

    private static final TimeWindowResolverImpl TIME_WINDOW_RESOLVER = new TimeWindowResolverImpl();

    public static <U> HistoryCacheKey<U> generate(String function,
                                                  Time_windowContext timeWindow,
                                                  Group_byContext groupBy,
                                                  List<TerminalNode> fields,
                                                  TerminalNode errorCode,
                                                  Function<String, U> resolve) {
        var window = TIME_WINDOW_RESOLVER.resolve(timeWindow);
        List<U> resolvedFields = resolveFields(fields, resolve);
        List<U> groups = groupBy == null || groupBy.string_list() == null ? List.of()
                : resolveFields(groupBy.string_list().STRING(), resolve);
        String resolvedErrorCode = errorCode == null ? null : TextUtil.safeGetText(errorCode);
        return HistoryCacheKey.<U>builder()
                .function(function)
                .fields(resolvedFields)
                .start(window.getStart())
                .end(window.getEnd())
                .timeUnit(window.getTimeUnit())
                .errorCode(resolvedErrorCode)
                .groupBy(groups)
                .build();
    }

    private static <U> List<U> resolveFields(List<TerminalNode> fields, Function<String, U> resolve) {
        return fields.stream()
                .map(field -> resolve.apply(TextUtil.safeGetText(field)))
                .toList();
    }
}
