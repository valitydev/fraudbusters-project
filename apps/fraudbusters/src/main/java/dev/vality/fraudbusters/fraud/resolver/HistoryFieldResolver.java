package dev.vality.fraudbusters.fraud.resolver;

import dev.vality.fraudbusters.fraud.model.FieldModel;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Maps a rule field of one operation type to a column of an operation history table.
 * A field the history does not store must be rejected, never resolved to an empty filter.
 */
public interface HistoryFieldResolver<T, U> {

    FieldModel resolve(U field, T model);

    String resolve(U field);

    default List<FieldModel> resolveListFields(T model, List<U> fields) {
        if (fields == null) {
            return new ArrayList<>();
        }
        return fields.stream()
                .map(field -> resolve(field, model))
                .collect(Collectors.toList());
    }

}
