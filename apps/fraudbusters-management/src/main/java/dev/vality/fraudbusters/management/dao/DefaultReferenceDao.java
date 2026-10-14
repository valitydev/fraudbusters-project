package dev.vality.fraudbusters.management.dao;

import dev.vality.fraudbusters.management.domain.DefaultReferenceModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;

import java.util.List;

public interface DefaultReferenceDao<T extends DefaultReferenceModel> {

    void insert(T referenceModel);

    void remove(String id);

    T getById(String id);

    List<T> filterReferences(FilterRequest filterRequest);

    Integer countFilterModel(String searchValue);
}
