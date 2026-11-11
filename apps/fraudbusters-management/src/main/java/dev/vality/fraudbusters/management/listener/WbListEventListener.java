package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.wb_list.Event;
import dev.vality.dao.DaoException;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WbListEventListener {

    private final InspectStorageRegistry inspectStorageRegistry;
    private final AuditService auditService;

    @KafkaListener(topics = "${kafka.topic.wblist.event.sink}", containerFactory = "kafkaListenerContainerFactory")
    public void listen(Event event) throws DaoException {
        log.info("WbListListener event: {}", event);
        ModelProjection<Event, ?> listProjection = inspectStorageRegistry.findStorageByEvent(event)
                .lists();
        switch (event.getEventType()) {
            case CREATED -> listProjection.convertAndCreate(event);
            case DELETED -> listProjection.convertAndDelete(event);
            default -> log.warn("WbListListener event for list not found! event: {}", event);
        }
        auditService.logEvent(event);
    }

}
