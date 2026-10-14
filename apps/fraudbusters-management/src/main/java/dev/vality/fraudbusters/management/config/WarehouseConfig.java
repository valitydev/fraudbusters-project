package dev.vality.fraudbusters.management.config;

import dev.vality.fraudbusters.warehouse.QueryServiceSrv;
import dev.vality.woody.thrift.impl.http.THSpawnClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.IOException;

@Configuration
public class WarehouseConfig {

    @Bean
    public QueryServiceSrv.Iface bouncerClient(@Value("${service.warehouse.url}") Resource resource,
                                               @Value("${service.warehouse.networkTimeout}") int networkTimeout)
            throws IOException {
        return new THSpawnClientBuilder()
                .withNetworkTimeout(networkTimeout)
                .withAddress(resource.getURI())
                .withLogEnabled(true)
                .build(QueryServiceSrv.Iface.class);
    }

}
