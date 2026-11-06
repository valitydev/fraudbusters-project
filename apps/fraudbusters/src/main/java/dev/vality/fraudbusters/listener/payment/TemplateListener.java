package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.fraudbusters.fraud.FraudContextParser;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.TemplateCommandListener;
import dev.vality.fraudo.FraudoRulesParser;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
public class TemplateListener extends TemplateCommandListener {

    public TemplateListener(FraudContextParser<FraudoRulesParser.ParseContext> rulesContextParser,
                            InspectStorage paymentInspectStorage) {
        super(rulesContextParser, paymentInspectStorage);
    }

    @Override
    @KafkaListener(topics = "${kafka.topic.template}", containerFactory = "templateListenerContainerFactory")
    public void listen(@Payload Command command) {
        handle(command);
    }

}
