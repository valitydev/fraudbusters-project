package dev.vality.fraudbusters.management.dao;

/** Stores the model of a rule configuration command: CREATE inserts it, DELETE removes it. */
public interface CommandDao<T> {

    void insert(T model);

    void remove(T model);

}
