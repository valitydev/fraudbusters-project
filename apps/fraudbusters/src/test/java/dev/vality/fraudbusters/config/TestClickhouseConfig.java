package dev.vality.fraudbusters.config;

import com.clickhouse.client.api.ClientConfigProperties;
import com.clickhouse.jdbc.ClickHouseDataSource;
import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Properties;

@Slf4j
@TestConfiguration
@RequiredArgsConstructor
public class TestClickhouseConfig {

    @Bean
    @Primary
    public ClickHouseDataSource clickHouseDataSource() throws SQLException {
        Integer firstMappedPort = ClickHouseContainerExtension.CLICKHOUSE_CONTAINER.getFirstMappedPort();
        log.info("{}", firstMappedPort);
        Properties info = new Properties();
        info.put(ClientConfigProperties.USER.getKey(), "default");
        info.put(ClientConfigProperties.COMPRESS_SERVER_RESPONSE.getKey(), Boolean.FALSE.toString());
        return new ClickHouseDataSource("jdbc:clickhouse://localhost:" + firstMappedPort + "/default", info);
    }

    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource clickHouseDataSource) {
        return new JdbcTemplate(clickHouseDataSource);
    }

    @Bean
    public JdbcTemplate longQueryJdbcTemplate() throws SQLException {
        Properties info = new Properties();
        info.put(ClientConfigProperties.USER.getKey(), "default");
        info.put(ClientConfigProperties.COMPRESS_SERVER_RESPONSE.getKey(), Boolean.TRUE.toString());
        info.put(ClientConfigProperties.CONNECTION_TIMEOUT.getKey(), "10000");
        info.put(ClientConfigProperties.SOCKET_OPERATION_TIMEOUT.getKey(), "10000");
        Integer firstMappedPort = ClickHouseContainerExtension.CLICKHOUSE_CONTAINER.getFirstMappedPort();
        return new JdbcTemplate(
                new ClickHouseDataSource("jdbc:clickhouse://localhost:" + firstMappedPort + "/default", info));
    }

    @Bean
    @Primary
    public NamedParameterJdbcTemplate namedParameterJdbcTemplate(ClickHouseDataSource clickHouseDataSource) {
        return new NamedParameterJdbcTemplate(clickHouseDataSource);
    }

}
