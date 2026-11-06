package dev.vality.wb.list.manager.config;

import dev.vality.damsel.wb_list.WbListServiceSrv;
import dev.vality.wb.list.manager.handler.WbListServiceHandler;
import dev.vality.wb.list.manager.storage.ListStorageRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
public class ResourceConfig {

    @Bean
    public WbListServiceSrv.Iface fraudInspectorHandler(ListStorageRegistry listStorageRegistry,
                                                        JsonMapper objectMapper) {
        return new WbListServiceHandler(listStorageRegistry, objectMapper);
    }

}
