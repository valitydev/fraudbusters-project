package dev.vality.fraudbusters.management.dao;

import dev.vality.fraudbusters.management.domain.ReferenceModel;

public interface ReferenceDao<T extends ReferenceModel> extends CommandDao<T> {

    void remove(String id);

    T getById(String id);

    T getGlobalReference();

}
