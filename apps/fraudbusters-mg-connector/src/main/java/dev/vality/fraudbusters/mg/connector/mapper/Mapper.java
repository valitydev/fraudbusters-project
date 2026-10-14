package dev.vality.fraudbusters.mg.connector.mapper;


import dev.vality.fraudbusters.mg.connector.constant.EventType;

public interface Mapper<C, P, R> {

    String DELIMITER = ".";

    default boolean accept(C change) {
        return getChangeType().getFilter().match(change);
    }

    R map(C change, P parent);

    EventType getChangeType();

}
