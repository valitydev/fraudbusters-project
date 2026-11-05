package dev.vality.fraudbusters.fraud.resolver;

import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudo.exception.UnresolvableFieldException;
import dev.vality.fraudo.resolver.FieldResolver;
import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

/**
 * Resolves a rule field against one history table: the table names the column by the rule field name,
 * the operation's own field resolver gives the value. A field the table does not store is rejected.
 */
@RequiredArgsConstructor
public class HistoryFieldResolverImpl<T, U> implements HistoryFieldResolver<T, U> {

    private final Function<U, String> fieldNameResolver;
    private final Function<String, Optional<String>> columnResolver;
    private final FieldResolver<T, U> valueResolver;

    @Override
    public FieldModel resolve(U field, T model) {
        return new FieldModel(resolve(field), valueResolver.resolveValue(fieldNameResolver.apply(field), model));
    }

    @Override
    public String resolve(U field) {
        if (field == null) {
            throw new UnresolvableFieldException("null");
        }
        String fieldName = fieldNameResolver.apply(field);
        return columnResolver.apply(fieldName)
                .orElseThrow(() -> new UnresolvableFieldException(fieldName));
    }

    public boolean isSupported(U field) {
        return columnResolver.apply(fieldNameResolver.apply(field))
                .isPresent();
    }

}
