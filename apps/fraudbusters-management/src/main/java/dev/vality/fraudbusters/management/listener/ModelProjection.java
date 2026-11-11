package dev.vality.fraudbusters.management.listener;

import dev.vality.fraudbusters.management.dao.CdDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;

import java.util.function.Consumer;

/** Converts a command or list event to its model and creates or removes it in one table of an operation type. */
@RequiredArgsConstructor
public class ModelProjection<S, T> {

    private final Converter<S, T> converter;
    private final Consumer<T> createConsumer;
    private final Consumer<T> deleteConsumer;

    public static <S, T> ModelProjection<S, T> fromCommandDao(Converter<S, T> converter, CommandDao<T> dao) {
        return new ModelProjection<>(converter, dao::insert, dao::remove);
    }

    public static <S, T> ModelProjection<S, T> fromListDao(Converter<S, T> converter, CdDao<T> dao) {
        return new ModelProjection<>(converter, dao::saveListRecord, dao::removeRecord);
    }

    public void convertAndCreate(S source) {
        createConsumer.accept(converter.convert(source));
    }

    public void convertAndDelete(S source) {
        deleteConsumer.accept(converter.convert(source));
    }
}
