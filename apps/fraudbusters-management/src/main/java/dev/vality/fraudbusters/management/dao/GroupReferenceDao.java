package dev.vality.fraudbusters.management.dao;

import dev.vality.fraudbusters.management.domain.GroupReferenceModel;

import java.util.List;

public interface GroupReferenceDao<T extends GroupReferenceModel> extends CommandDao<T> {

    List<T> getByGroupId(String id);

}
