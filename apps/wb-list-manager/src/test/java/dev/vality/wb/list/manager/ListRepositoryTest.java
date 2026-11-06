package dev.vality.wb.list.manager;

import dev.vality.wb.list.manager.model.Row;
import dev.vality.wb.list.manager.repository.ListRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ListRepositoryTest {

    private static final String VALUE = "value";
    private static final String KEY = "key";

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ListRepository listRepository;

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine");

    @BeforeEach
    public void setUp() throws SQLException {
        jdbcTemplate.execute("truncate table wb_list.row;");
    }

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @Test
    void dbTest() {
        Row row = new Row();
        row.setKey(KEY);
        row.setValue(VALUE);
        await().pollDelay(5_000L, TimeUnit.MILLISECONDS)
                .atMost(30_000L, TimeUnit.MILLISECONDS)
                .ignoreExceptions()
                .until(() -> {
                    listRepository.create(row);
                    return listRepository.get(KEY).isPresent();
                });

        Optional<Row> resultGet = listRepository.get(KEY);
        assertFalse(resultGet.isEmpty());
        assertEquals(VALUE, resultGet.get().getValue());

        listRepository.remove(row);
        resultGet = listRepository.get(KEY);

        assertTrue(resultGet.isEmpty());
    }
}
