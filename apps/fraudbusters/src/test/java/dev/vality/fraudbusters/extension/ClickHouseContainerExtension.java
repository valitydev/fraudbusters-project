package dev.vality.fraudbusters.extension;

import com.clickhouse.jdbc.ClickHouseDataSource;
import dev.vality.fraudbusters.config.ClickhouseFlywayFactory;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.ClickHouseContainer;
import org.testcontainers.utility.MountableFile;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class ClickHouseContainerExtension implements BeforeAllCallback, AfterAllCallback {

    private static final String VERSION = "clickhouse/clickhouse-server:25.7.5";
    private static final String REPLICATED_CLUSTER_CONFIG = "clickhouse/replicated-cluster.xml";
    private static final String REPLICATED_CLUSTER_CONFIG_PATH =
            "/etc/clickhouse-server/config.d/replicated-cluster.xml";

    public static ClickHouseContainer CLICKHOUSE_CONTAINER;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        CLICKHOUSE_CONTAINER = new ClickHouseContainer(VERSION);
        CLICKHOUSE_CONTAINER.withEnv("CLICKHOUSE_SKIP_USER_SETUP", "1");
        CLICKHOUSE_CONTAINER.withCopyFileToContainer(MountableFile.forClasspathResource(REPLICATED_CLUSTER_CONFIG),
                REPLICATED_CLUSTER_CONFIG_PATH);
        CLICKHOUSE_CONTAINER.start();
        log.info("{}", CLICKHOUSE_CONTAINER.getFirstMappedPort());
        ClickhouseFlywayFactory.createFlyway(createDataSource())
                .migrate();
    }

    @SneakyThrows
    public static ClickHouseDataSource createDataSource() {
        return new ClickHouseDataSource("jdbc:clickhouse://localhost:" +
                CLICKHOUSE_CONTAINER.getFirstMappedPort() + "/default");
    }

    public static void executeScripts(List<String> scripts) {
        JdbcTemplate jdbc = new JdbcTemplate(createDataSource());
        scripts.stream()
                .map(ClickHouseContainerExtension::readScript)
                .flatMap(script -> Arrays.stream(script.split(";")))
                .map(String::trim)
                .filter(statement -> !statement.isEmpty())
                .forEach(jdbc::execute);
    }

    @SneakyThrows
    private static String readScript(String path) {
        return new ClassPathResource(path).getContentAsString(StandardCharsets.UTF_8);
    }

    public static void removeEventsRetention() {
        new JdbcTemplate(createDataSource())
                .execute("ALTER TABLE fraud.events_unique REMOVE TTL");
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) {
        CLICKHOUSE_CONTAINER.stop();
    }
}
