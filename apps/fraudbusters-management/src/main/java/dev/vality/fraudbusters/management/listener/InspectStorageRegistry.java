package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.exception.UnknownEventException;
import dev.vality.fraudbusters.management.utils.OperationTypeUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InspectStorageRegistry {

    private final Map<OperationType, InspectStorage> storagesByType = new EnumMap<>(OperationType.class);

    public InspectStorageRegistry(List<InspectStorage> storages) {
        for (InspectStorage storage : storages) {
            if (storagesByType.putIfAbsent(storage.operationType(), storage) != null) {
                throw new IllegalStateException(
                        "Duplicate inspect storage for operation type " + storage.operationType());
            }
        }
    }

    public InspectStorage get(Command command) {
        return get(OperationTypeUtil.resolve(command));
    }

    /** A list row without an identifier keeps its historical meaning: payment. */
    public InspectStorage get(Event event) {
        Row row = event.getRow();
        if (!row.isSetId() || row.getId().isSetPaymentId()) {
            return get(OperationType.payment);
        }
        if (row.getId().isSetPayoutId()) {
            return get(OperationType.payout);
        }
        log.error("Unknown event when wbListEventListener listen event: {}", event);
        throw new UnknownEventException(
                String.format("Unknown event when wbListEventListener listen event: %s", event));
    }

    private InspectStorage get(OperationType operationType) {
        InspectStorage storage = storagesByType.get(operationType);
        if (storage == null) {
            throw new IllegalArgumentException("Unsupported operation type: " + operationType);
        }
        return storage;
    }

}
