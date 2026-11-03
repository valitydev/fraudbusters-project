package dev.vality.fraudbusters.fraud;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.fraudbusters.util.OperationTypeUtil;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

@Component
public class InspectStorageRegistry {

    private final Map<OperationType, InspectStorage> storagesByType = new EnumMap<>(OperationType.class);

    public InspectStorageRegistry(List<InspectStorage> storages) {
        for (InspectStorage storage : storages) {
            if (storagesByType.putIfAbsent(storage.getOperationType(), storage) != null) {
                throw new IllegalStateException(
                        "Duplicate inspect storage for operation type " + storage.getOperationType());
            }
        }
    }

    public InspectStorage get(Command command) {
        return get(OperationTypeUtil.resolve(command));
    }

    /** A missing operation type keeps its historical meaning: payment. */
    public InspectStorage get(OperationType operationType) {
        InspectStorage storage = storagesByType.get(operationType == null ? OperationType.payment : operationType);
        if (storage == null) {
            throw new IllegalArgumentException("Unsupported operation type: " + operationType);
        }
        return storage;
    }

}
