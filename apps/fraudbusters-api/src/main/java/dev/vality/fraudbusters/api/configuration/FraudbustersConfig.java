package dev.vality.fraudbusters.api.configuration;

import dev.vality.damsel.fraudbusters.InspectorServiceSrv;
import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.damsel.proxy_inspector.InspectorProxySrv;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class FraudbustersConfig {

    @Bean
    public PaymentServiceSrv.Iface paymentServiceSrv(
            @Value("${fraudbusters.service.payment.url}") Resource resource,
            @Value("${fraudbusters.service.payment.networkTimeout}") int networkTimeout) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(PaymentServiceSrv.Iface.class);
    }

    @Bean
    public InspectorServiceSrv.Iface inspectorServiceSrv(
            @Value("${fraudbusters.service.user.inspector.url}") Resource resource,
            @Value("${fraudbusters.service.user.inspector.networkTimeout}") int networkTimeout) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(InspectorServiceSrv.Iface.class);
    }

    @Bean
    public InspectorProxySrv.Iface proxyInspectorSrv(
            @Value("${fraudbusters.service.inspector.url}") Resource resource,
            @Value("${fraudbusters.service.inspector.networkTimeout}") int networkTimeout) throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI()).build(InspectorProxySrv.Iface.class);
    }

}
