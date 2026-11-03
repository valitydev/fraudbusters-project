package dev.vality.wb.list.manager.storage;

import dev.vality.damsel.wb_list.Row;
import dev.vality.wb.list.manager.repository.ListRepository;

import java.util.List;

/** Table and key conventions of list rows with one type of identifier. */
public interface ListStorage {

    ListRepository getRepository();

    String key(Row row);

    /** Keys from the widest scope to the narrowest; the first stored row applies. */
    List<String> cascadeKeys(Row row);

}
