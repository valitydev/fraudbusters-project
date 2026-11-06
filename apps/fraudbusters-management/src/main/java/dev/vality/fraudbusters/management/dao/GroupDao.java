package dev.vality.fraudbusters.management.dao;

import dev.vality.fraudbusters.management.domain.GroupModel;
import org.codehaus.commons.nullanalysis.Nullable;

import java.util.List;

public interface GroupDao {

    void insert(GroupModel groupModel);

    void remove(String id);

    void remove(GroupModel groupModel);

    @Nullable
    GroupModel getById(String id);

    List<GroupModel> filterGroup(String filterValue);
}
