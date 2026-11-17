package dev.vality.fraudbusters.config;

import com.clickhouse.client.api.ClientConfigProperties;
import com.clickhouse.jdbc.ClickHouseDataSource;
import dev.vality.fraudbusters.config.properties.ClickhouseProperties;
import lombok.RequiredArgsConstructor;
import org.flywaydb.core.Flyway;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class ClickhouseConfig {

    private final ClickhouseProperties clickhouseProperties;

    @Bean
    public ClickHouseDataSource clickHouseDataSource() throws SQLException {
        Properties info = new Properties();
        info.put(ClientConfigProperties.USER.getKey(), clickhouseProperties.getUser());
        info.put(ClientConfigProperties.PASSWORD.getKey(), clickhouseProperties.getPassword());
        info.put(ClientConfigProperties.COMPRESS_SERVER_RESPONSE.getKey(), clickhouseProperties.getCompress());
        return new ClickHouseDataSource(clickhouseProperties.getUrl(), info);
    }

    @Bean
    public JdbcTemplate jdbcTemplate(DataSource clickHouseDataSource) {
        return new JdbcTemplate(clickHouseDataSource);
    }

    @Bean
    public JdbcTemplate longQueryJdbcTemplate() throws SQLException {
        Properties info = new Properties();
        info.put(ClientConfigProperties.USER.getKey(), clickhouseProperties.getUser());
        info.put(ClientConfigProperties.PASSWORD.getKey(), clickhouseProperties.getPassword());
        info.put(ClientConfigProperties.COMPRESS_SERVER_RESPONSE.getKey(), Boolean.TRUE.toString());
        info.put(ClientConfigProperties.CONNECTION_TIMEOUT.getKey(), clickhouseProperties.getConnectionTimeout());
        info.put(ClientConfigProperties.SOCKET_OPERATION_TIMEOUT.getKey(), clickhouseProperties.getSocketTimeout());
        return new JdbcTemplate(new ClickHouseDataSource(clickhouseProperties.getUrl(), info));
    }

    @Bean
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(ClickHouseDataSource clickHouseDataSource) {
        return new NamedParameterJdbcTemplate(clickHouseDataSource);
    }

    @Bean
    @ConditionalOnProperty(prefix = "clickhouse.flyway", name = "enabled", havingValue = "true", matchIfMissing = true)
    public Flyway clickHouseFlyway(
            ClickHouseDataSource clickHouseDataSource,
            @Value("${clickhouse.flyway.repair-before-migrate:false}") boolean repairBeforeMigrate) {
        Flyway flyway = ClickhouseFlywayFactory.createFlyway(clickHouseDataSource);
        if (repairBeforeMigrate) {
            flyway.repair();
        }
        flyway.migrate();
        return flyway;
    }

}
