package dev.vality.wb.list.manager.storage;

import dev.vality.damsel.wb_list.Row;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ListStorageRegistry {

    private final ListStorage paymentListStorage;
    private final ListStorage payoutListStorage;

    /** A row without an identifier keeps its historical meaning: payment. */
    public ListStorage findStorageByRow(Row row) {
        if (row.isSetId() && row.getId().isSetPayoutId()) {
            return payoutListStorage;
        }
        if (row.isSetId() && row.getId().isSetP2pId()) {
            throw new IllegalStateException("P2P is not supported. Row: " + row);
        }
        return paymentListStorage;
    }

}
