package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.GroupReference;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.fraud.InspectStorageRegistry;
import dev.vality.fraudbusters.listener.AbstractPoolCommandListenerExecutor;
import dev.vality.fraudbusters.listener.CommandListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GroupReferenceListener extends AbstractPoolCommandListenerExecutor implements CommandListener {

    private final InspectStorageRegistry inspectStorageRegistry;

    @Override
    @KafkaListener(topics = "${kafka.topic.group-reference}",
            containerFactory = "groupReferenceListenerContainerFactory")
    public void listen(@Payload Command command) {
        log.info("GroupReferenceListener command: {}", command);
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetGroupReference()) {
            GroupReference reference = command.getCommandBody().getGroupReference();
            InspectStorage storage = inspectStorageRegistry.get(command);
            String key = storage.groupReferenceKey(reference);
            execCommand(command, key, storage.getGroupReferencePool(), reference::getGroupId);
        }
    }

}
