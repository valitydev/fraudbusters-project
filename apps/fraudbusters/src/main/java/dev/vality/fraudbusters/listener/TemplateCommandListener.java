package dev.vality.fraudbusters.listener;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.Template;
import dev.vality.fraudbusters.fraud.FraudContextParser;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudo.FraudoRulesParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;

import java.nio.charset.StandardCharsets;

/** Keeps the templates of the operation type whose topic the subclass listens to. */
@Slf4j
@RequiredArgsConstructor
public abstract class TemplateCommandListener extends AbstractPoolCommandListenerExecutor implements CommandListener {

    private final FraudContextParser<FraudoRulesParser.ParseContext> rulesContextParser;
    private final InspectStorage storage;

    protected void handle(Command command) {
        log.info("{} command: {}", getClass().getSimpleName(), command);
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetTemplate()) {
            Template template = command.getCommandBody().getTemplate();
            execCommand(command, template.getId(), storage.getTemplatePool(), () -> parseTemplate(template));
        }
    }

    private ParserRuleContext parseTemplate(Template template) {
        String templateString = new String(template.getTemplate(), StandardCharsets.UTF_8);
        log.info("TemplateListener templateString: {}", templateString);
        storage.validateTemplate(template.getId(), templateString);
        return rulesContextParser.parse(templateString);
    }

}
