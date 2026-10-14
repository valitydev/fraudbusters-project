package dev.vality.fraudbusters.warehouse.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "clickhouse.db")
public class ClickhouseProperties {

    private String url;
    private String user;
    private String password;
    private String connectionTimeout;
    private String compress;

}
