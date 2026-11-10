package dev.vality.fraudbusters.pool.handler;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.Template;
import dev.vality.fraudbusters.fraud.FraudContextParser;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.listener.AbstractPoolCommandListenerExecutor;
import dev.vality.fraudo.FraudoRulesParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.antlr.v4.runtime.ParserRuleContext;

import java.nio.charset.StandardCharsets;

/**
 * Applies template commands to the template pool of one operation type; payments and payouts each get a bean.
 */
@Slf4j
@RequiredArgsConstructor
public class TemplateCommandHandler extends AbstractPoolCommandListenerExecutor {

    private final FraudContextParser<FraudoRulesParser.ParseContext> rulesContextParser;
    private final InspectStorage storage;

    public void handle(Command command) {
        if (command != null && command.isSetCommandBody() && command.getCommandBody().isSetTemplate()) {
            Template template = command.getCommandBody().getTemplate();
            execCommand(command, template.getId(), storage.getTemplatePool(), () -> parseTemplate(template));
        }
    }

    private ParserRuleContext parseTemplate(Template template) {
        String templateString = new String(template.getTemplate(), StandardCharsets.UTF_8);
        log.info("TemplateCommandHandler templateString: {}", templateString);
        storage.validateTemplate(template.getId(), templateString);
        return rulesContextParser.parse(templateString);
    }

}
