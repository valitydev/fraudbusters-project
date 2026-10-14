package dev.vality.fraudbusters.mg.connector.config;

import dev.vality.damsel.domain_config_v2.RepositoryClientSrv;
import dev.vality.damsel.payment_processing.InvoicingSrv;
import dev.vality.fistful.withdrawal.ManagementSrv;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class HgConfig {

    @Bean
    public InvoicingSrv.Iface invoicingClient(@Value("${service.invoicing.url}") Resource resource,
                                              @Value("${service.invoicing.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI())
                .build(InvoicingSrv.Iface.class);
    }

    @Bean
    public ManagementSrv.Iface withdrawalClient(
            @Value("${service.withdrawal.url}") Resource resource,
            @Value("${service.withdrawal.networkTimeout}") int networkTimeout) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI())
                .build(ManagementSrv.Iface.class);
    }

    @Bean
    public dev.vality.fistful.destination.ManagementSrv.Iface destinationClient(
            @Value("${service.destination.url}") Resource resource,
            @Value("${service.destination.networkTimeout}") int networkTimeout) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI())
                .build(dev.vality.fistful.destination.ManagementSrv.Iface.class);
    }

    @Bean
    public RepositoryClientSrv.Iface dominantClient(
            @Value("${service.dominant.url}") Resource resource,
            @Value("${service.dominant.networkTimeout}") int networkTimeout) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI())
                .build(RepositoryClientSrv.Iface.class);
    }

}
