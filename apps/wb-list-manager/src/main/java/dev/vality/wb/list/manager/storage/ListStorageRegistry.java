package dev.vality.wb.list.manager.storage;

import dev.vality.damsel.wb_list.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ListStorageRegistry {

    private final List<ListStorage> listStorages;

    public ListStorage findStorageByRow(Row row) {
        return listStorages.stream()
                .filter(storage -> storage.canHandle(row))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unsupported list row: " + row));
    }

}
