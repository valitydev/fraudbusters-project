package dev.vality.fraudbusters.config;

import lombok.experimental.UtilityClass;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;
import java.util.Map;

@UtilityClass
public class ClickhouseFlywayFactory {

    private static final String FRAUD_DATABASE = "fraud";
    private static final String SCHEMA_HISTORY_TABLE = "clickhouse_flyway_schema_history";

    private static final String BASELINE_VERSION = "1";
    private static final String MIGRATION_LOCATION = "classpath:db/migration-clickhouse";

    private static final Map<String, String> REPLICATED_SCHEMA_HISTORY = Map.of(
            "flyway.clickhouse.clusterName", "'{cluster}'",
            "flyway.clickhouse.zookeeperPath", "/clickhouse/{cluster}/tables/{shard}/{database}/{table}");

    public static Flyway createFlyway(DataSource dataSource) {
        return Flyway.configure()
                .configuration(REPLICATED_SCHEMA_HISTORY)
                .dataSource(dataSource)
                .locations(MIGRATION_LOCATION)
                .schemas(FRAUD_DATABASE)
                .table(SCHEMA_HISTORY_TABLE)
                .baselineOnMigrate(true)
                .baselineVersion(BASELINE_VERSION)
                .load();
    }
}
