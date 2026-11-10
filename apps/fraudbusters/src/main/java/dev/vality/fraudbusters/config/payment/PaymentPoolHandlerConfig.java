package dev.vality.fraudbusters.config.payment;

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
 * Handlers that apply payment template, group and reference commands to the payment pools.
 */
@Configuration
public class PaymentPoolHandlerConfig {

    @Bean
    public TemplateCommandHandler paymentTemplateCommandHandler(
            FraudContextParser<FraudoRulesParser.ParseContext> rulesContextParser,
            InspectStorage paymentInspectStorage) {
        return new TemplateCommandHandler(rulesContextParser, paymentInspectStorage);
    }

    @Bean
    public TemplateReferenceCommandHandler paymentTemplateReferenceCommandHandler(
            InspectStorage paymentInspectStorage) {
        return new TemplateReferenceCommandHandler(paymentInspectStorage);
    }

    @Bean
    public GroupCommandHandler paymentGroupCommandHandler(InspectStorage paymentInspectStorage) {
        return new GroupCommandHandler(paymentInspectStorage);
    }

    @Bean
    public GroupReferenceCommandHandler paymentGroupReferenceCommandHandler(InspectStorage paymentInspectStorage) {
        return new GroupReferenceCommandHandler(paymentInspectStorage);
    }

}
