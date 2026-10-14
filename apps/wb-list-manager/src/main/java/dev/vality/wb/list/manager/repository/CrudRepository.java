package dev.vality.wb.list.manager.repository;

import dev.vality.wb.list.manager.model.Row;

import java.util.Optional;

public interface CrudRepository<T, K> {

    void create(T row);

    void remove(T row);

    Optional<Row> get(K key);

}
