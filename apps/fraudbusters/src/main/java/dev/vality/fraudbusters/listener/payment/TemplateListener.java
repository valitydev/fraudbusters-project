package dev.vality.fraudbusters.listener.payment;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.Template;
import dev.vality.fraudbusters.fraud.FraudContextParser;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.fraud.InspectStorageRegistry;
import dev.vality.fraudbusters.listener.AbstractPoolCommandListenerExecutor;
import dev.vality.fraudbusters.listener.CommandListener;
import dev.vality.fraudo.FraudoRulesParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;


@Slf4j
@Component
@RequiredArgsConstructor
public class TemplateListener extends AbstractPoolCommandListenerExecutor implements CommandListener {

    private final FraudContextParser<FraudoRulesParser.ParseContext> paymentContextParser;
    private final InspectStorageRegistry inspectStorageRegistry;

    @Override
    @KafkaListener(topics = "${kafka.topic.template}", containerFactory = "templateListenerContainerFactory")
    public void listen(@Payload Command command) {
        log.info("TemplateListener command: {}", command);
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetTemplate()) {
            Template template = command.getCommandBody().getTemplate();
            InspectStorage storage = inspectStorageRegistry.get(command);
            execCommand(command, template.getId(), storage.getTemplatePool(), () -> parseTemplate(storage, template));
        }
    }

    private ParserRuleContext parseTemplate(InspectStorage storage, Template template) {
        String templateString = new String(template.getTemplate(), StandardCharsets.UTF_8);
        log.info("TemplateListener templateString: {}", templateString);
        storage.validateTemplate(template.getId(), templateString);
        return paymentContextParser.parse(templateString);
    }

}
