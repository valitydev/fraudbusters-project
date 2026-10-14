package dev.vality.fraudo.payment.resolver;

import dev.vality.fraudo.resolver.FieldResolver;
import dev.vality.fraudo.resolver.GroupFieldsResolver;
import dev.vality.fraudo.utils.TextUtil;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.fraudo.FraudoPaymentParser.Group_byContext;

@RequiredArgsConstructor
public class PaymentGroupResolver<T, U> implements GroupFieldsResolver<Group_byContext, U> {

    private final FieldResolver<T, U> fieldResolver;

    public List<U> resolve(Group_byContext groupByContext) {
        List<U> fields = new ArrayList<>();
        if (groupByContext != null
                && groupByContext.string_list() != null
                && groupByContext.string_list().STRING() != null
                && !groupByContext.string_list().STRING().isEmpty()) {
            fields = groupByContext.string_list().STRING().stream()
                    .map(terminalNode -> fieldResolver.resolveName(TextUtil.safeGetText(terminalNode)))
                    .collect(Collectors.toList());
        }
        return fields;
    }

}
