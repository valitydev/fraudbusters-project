package dev.vality.fraudbusters.management.config;

import dev.vality.damsel.fraudbusters.HistoricalDataServiceSrv;
import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.fraudbusters.management.converter.TemplateModelToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.GroupToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.PaymentGroupReferenceModelToCommandConverter;
import dev.vality.fraudbusters.management.service.CommandSender;
import dev.vality.fraudbusters.management.service.TemplateCommandService;
import dev.vality.fraudbusters.management.service.payment.PaymentGroupCommandService;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class PaymentFraudoConfig {

    @Bean
    public PaymentServiceSrv.Iface paymentServiceSrv(@Value("${service.payment.url}") Resource resource,
                                                     @Value("${service.payment.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(PaymentServiceSrv.Iface.class);
    }

    @Bean
    public HistoricalDataServiceSrv.Iface historicalDataServiceSrv(
            @Value("${service.historical.url}") Resource resource,
            @Value("${service.historical.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(HistoricalDataServiceSrv.Iface.class);
    }

    @Bean
    public TemplateCommandService paymentTemplateCommandService(
            CommandSender commandSender,
            TemplateModelToCommandConverter templateModelToCommandConverter,
            @Value("${kafka.topic.fraudbusters.payment.template}") String topic) {
        return new TemplateCommandService(commandSender, topic, templateModelToCommandConverter);
    }

    @Bean
    public PaymentGroupCommandService paymentGroupCommandService(
            CommandSender commandSender,
            @Value("${kafka.topic.fraudbusters.payment.group.list}")
                    String topic,
            GroupToCommandConverter groupToCommandConverter,
            PaymentGroupReferenceModelToCommandConverter groupReferenceToCommandConverter) {
        return new PaymentGroupCommandService(commandSender, topic, groupToCommandConverter,
                groupReferenceToCommandConverter);
    }

}
