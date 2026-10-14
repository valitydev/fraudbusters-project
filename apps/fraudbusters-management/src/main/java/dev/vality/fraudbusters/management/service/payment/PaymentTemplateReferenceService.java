package dev.vality.fraudbusters.management.service.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.management.service.CommandSender;
import dev.vality.fraudbusters.management.utils.ReferenceKeyGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentTemplateReferenceService {

    private final CommandSender commandSender;

    @Value("${kafka.topic.fraudbusters.payment.reference}")
    public String topic;

    public String sendCommandSync(Command command) {
        String key = ReferenceKeyGenerator.generateTemplateKey(command.getCommandBody().getReference());
        return commandSender.send(topic, command, key);
    }

}
