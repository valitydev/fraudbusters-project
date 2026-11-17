package dev.vality.fraudbusters.warehouse.config;

import com.clickhouse.client.api.ClientConfigProperties;
import com.clickhouse.jdbc.ClickHouseDataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import java.sql.SQLException;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ClickhouseConfig {

    private final ClickhouseProperties properties;

    public ClickHouseDataSource clickHouseDataSource() throws SQLException {
        Properties info = new Properties();
        info.setProperty(ClientConfigProperties.USER.getKey(), properties.getUser());
        info.setProperty(ClientConfigProperties.PASSWORD.getKey(), properties.getPassword());
        info.setProperty(ClientConfigProperties.COMPRESS_SERVER_RESPONSE.getKey(), properties.getCompress());
        info.setProperty(ClientConfigProperties.CONNECTION_TIMEOUT.getKey(), properties.getConnectionTimeout());
        return new ClickHouseDataSource(properties.getUrl(), info);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() throws SQLException {
        return new NamedParameterJdbcTemplate(clickHouseDataSource());
    }

}
