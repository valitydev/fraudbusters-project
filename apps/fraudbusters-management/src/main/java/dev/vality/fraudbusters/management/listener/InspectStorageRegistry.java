package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.wb_list.Event;
import dev.vality.fraudbusters.management.exception.UnknownEventException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InspectStorageRegistry {

    private final List<InspectStorage> inspectStorages;

    public InspectStorage findStorageByEvent(Event event) {
        return inspectStorages.stream()
                .filter(storage -> storage.canHandle(event))
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Unknown event when wbListEventListener listen event: {}", event);
                    return new UnknownEventException(
                            String.format("Unknown event when wbListEventListener listen event: %s", event));
                });
    }

}
