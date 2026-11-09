package dev.vality.fraudbusters.config.payout;

import dev.vality.fraudbusters.fraud.FraudContextParser;
import dev.vality.fraudbusters.fraud.InspectStorage;
import dev.vality.fraudbusters.pool.handler.GroupCommandHandler;
import dev.vality.fraudbusters.pool.handler.GroupReferenceCommandHandler;
import dev.vality.fraudbusters.pool.handler.TemplateCommandHandler;
import dev.vality.fraudbusters.pool.handler.TemplateReferenceCommandHandler;
import dev.vality.fraudo.FraudoRulesParser;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Handlers that apply payout template, group and reference commands to the payout pools.
 */
@Configuration
public class PayoutPoolHandlerConfig {

    @Bean
    public TemplateCommandHandler payoutTemplateCommandHandler(
            FraudContextParser<FraudoRulesParser.ParseContext> rulesContextParser,
            InspectStorage payoutInspectStorage) {
        return new TemplateCommandHandler(rulesContextParser, payoutInspectStorage);
    }

    @Bean
    public TemplateReferenceCommandHandler payoutTemplateReferenceCommandHandler(InspectStorage payoutInspectStorage) {
        return new TemplateReferenceCommandHandler(payoutInspectStorage);
    }

    @Bean
    public GroupCommandHandler payoutGroupCommandHandler(InspectStorage payoutInspectStorage) {
        return new GroupCommandHandler(payoutInspectStorage);
    }

    @Bean
    public GroupReferenceCommandHandler payoutGroupReferenceCommandHandler(InspectStorage payoutInspectStorage) {
        return new GroupReferenceCommandHandler(payoutInspectStorage);
    }

}
