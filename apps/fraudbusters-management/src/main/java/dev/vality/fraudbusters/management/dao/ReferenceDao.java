package dev.vality.fraudbusters.management.dao;

import dev.vality.fraudbusters.management.domain.ReferenceModel;

public interface ReferenceDao<T extends ReferenceModel> {

    void insert(T referenceModel);

    void remove(String id);

    void remove(T referenceModel);

    T getById(String id);

    T getGlobalReference();

}
