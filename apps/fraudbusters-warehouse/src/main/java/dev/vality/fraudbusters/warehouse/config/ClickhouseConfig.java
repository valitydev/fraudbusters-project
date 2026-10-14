package dev.vality.fraudbusters.warehouse.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import ru.yandex.clickhouse.ClickHouseDataSource;
import ru.yandex.clickhouse.settings.ClickHouseQueryParam;

import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ClickhouseConfig {

    private final ClickhouseProperties properties;

    public ClickHouseDataSource clickHouseDataSource() {
        Properties info = new Properties();
        info.setProperty(ClickHouseQueryParam.USER.getKey(), properties.getUser());
        info.setProperty(ClickHouseQueryParam.PASSWORD.getKey(), properties.getPassword());
        info.setProperty(ClickHouseQueryParam.COMPRESS.getKey(), properties.getCompress());
        info.setProperty(ClickHouseQueryParam.CONNECT_TIMEOUT.getKey(), properties.getConnectionTimeout());
        return new ClickHouseDataSource(properties.getUrl(), info);
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate() {
        return new NamedParameterJdbcTemplate(clickHouseDataSource());
    }

}
