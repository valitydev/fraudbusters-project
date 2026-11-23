package dev.vality.fraudbusters.config;

import dev.vality.fraudbusters.extension.ClickHouseContainerExtension;
import org.flywaydb.core.api.output.MigrateResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ClickHouseContainerExtension.class)
class ClickhouseFlywayMigrationTest {

    private static final String CURRENT_PRODUCTION_SCHEMA_SCRIPT = "sql/current_production_schema.sql";
    private static final List<String> CLIENT_IDENTITY_TABLES =
            List.of("events_unique", "payment", "refund", "chargeback", "fraud_payment");

    private JdbcTemplate jdbc;

    @BeforeEach
    void dropFraudDatabase() {
        jdbc = new JdbcTemplate(ClickHouseContainerExtension.createDataSource());
        jdbc.execute("DROP DATABASE IF EXISTS fraud SYNC");
    }

    @Test
    void cleanDatabaseIsCreatedByAllMigrations() {
        assertEquals(5, migrate().migrationsExecuted);
        assertEquals(List.of("1 SQL", "2 SQL", "3 SQL", "4 SQL", "5 SQL"), findAppliedVersions());
        assertEquals(List.of(
                        "chargeback ReplicatedReplacingMergeTree",
                        "events_unique ReplicatedMergeTree",
                        "fraud_payment ReplicatedMergeTree",
                        "payment ReplicatedReplacingMergeTree",
                        "payout ReplicatedReplacingMergeTree",
                        "payout_check ReplicatedMergeTree",
                        "refund ReplicatedReplacingMergeTree"),
                jdbc.queryForList("""
                        SELECT concat(name, ' ', engine) FROM system.tables
                        WHERE database = 'fraud' AND name != 'clickhouse_flyway_schema_history'
                        ORDER BY name
                        """, String.class));
        assertEquals(List.of("events_unique"), jdbc.queryForList("""
                SELECT name FROM system.tables
                WHERE database = 'fraud' AND match(engine_full, '[[:space:]]TTL[[:space:]]')
                """, String.class));
        assertEquals(0, migrate().migrationsExecuted);
    }

    @Test
    void schemaHistoryIsReplicatedAcrossCluster() {
        migrate();

        assertEquals("/clickhouse/fraud_cluster/tables/01/fraud/clickhouse_flyway_schema_history",
                jdbc.queryForObject("""
                        SELECT zookeeper_path FROM system.replicas
                        WHERE database = 'fraud' AND table = 'clickhouse_flyway_schema_history'
                        """, String.class));
    }

    @Test
    void currentProductionDatabaseIsBaselinedAndUpgradedToCleanSchema() {
        migrate();
        final List<String> cleanSchema = describeFraudSchema();
        jdbc.execute("DROP DATABASE fraud SYNC");
        jdbc.execute("CREATE DATABASE fraud");
        ClickHouseContainerExtension.executeScripts(List.of(CURRENT_PRODUCTION_SCHEMA_SCRIPT));
        jdbc.update("""
                INSERT INTO fraud.withdrawal (timestamp, eventTime, eventTimeHour, id, status)
                VALUES ('2026-01-01', 1767225600000, 1767225600000, 'existing', 'succeeded')
                """);

        assertEquals(4, migrate().migrationsExecuted);

        assertEquals(List.of("1 BASELINE", "2 SQL", "3 SQL", "4 SQL", "5 SQL"), findAppliedVersions());
        assertEquals(cleanSchema, describeFraudSchema());
        assertEquals(1, jdbc.queryForObject("""
                SELECT count() FROM fraud.payout WHERE id = 'existing' AND partyId IS NULL
                """, Integer.class));
    }

    @Test
    void databaseWithoutClientIdentityIsBaselinedAndUpgradedToCleanSchema() {
        migrate();
        final List<String> cleanSchema = describeFraudSchema();
        jdbc.execute("DROP DATABASE fraud SYNC");
        jdbc.execute("CREATE DATABASE fraud");
        ClickHouseContainerExtension.executeScripts(List.of(CURRENT_PRODUCTION_SCHEMA_SCRIPT));
        CLIENT_IDENTITY_TABLES.forEach(table -> jdbc.execute(
                "ALTER TABLE fraud." + table + " DROP COLUMN customerId, DROP COLUMN userId"));

        assertEquals(4, migrate().migrationsExecuted);

        assertEquals(List.of("1 BASELINE", "2 SQL", "3 SQL", "4 SQL", "5 SQL"), findAppliedVersions());
        assertEquals(cleanSchema, describeFraudSchema());
    }

    private MigrateResult migrate() {
        return ClickhouseFlywayFactory.createFlyway(ClickHouseContainerExtension.createDataSource())
                .migrate();
    }

    private List<String> findAppliedVersions() {
        return jdbc.queryForList("""
                SELECT concat(version, ' ', type) FROM fraud.clickhouse_flyway_schema_history
                WHERE version IS NOT NULL AND success
                ORDER BY installed_rank
                """, String.class);
    }

    private List<String> describeFraudSchema() {
        return jdbc.queryForList("""
                SELECT concat(t.name, ' ', t.engine_full, ' ', c.name, ' ', c.type)
                FROM system.tables AS t
                INNER JOIN system.columns AS c ON c.database = t.database AND c.table = t.name
                WHERE t.database = 'fraud' AND t.name != 'clickhouse_flyway_schema_history'
                ORDER BY t.name, c.name
                """, String.class);
    }
}
