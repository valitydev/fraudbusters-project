package dev.vality.trusted.tokens.dao;

import dev.vality.trusted.tokens.dao.impl.TemplateDaoImpl;
import dev.vality.trusted.tokens.dao.impl.TokenDaoImpl;
import dev.vality.trusted.tokens.model.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import tools.jackson.databind.json.JsonMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class DaoPersistenceTest {

    @Container
    private static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:17")
            .withInitScript("db/migration/V1__init.sql");

    private TokenDaoImpl tokenDao;
    private TemplateDaoImpl templateDao;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        var dataSource = new PGSimpleDataSource();
        dataSource.setURL(POSTGRES.getJdbcUrl());
        dataSource.setUser(POSTGRES.getUsername());
        dataSource.setPassword(POSTGRES.getPassword());
        var mapper = JsonMapper.builder().build();
        tokenDao = new TokenDaoImpl(dataSource, mapper);
        templateDao = new TemplateDaoImpl(dataSource, mapper);
        jdbcTemplate = new JdbcTemplate(dataSource);
        jdbcTemplate.execute("TRUNCATE ttm.token_data, ttm.template_data");
    }

    @Test
    void tokenInsertReadAndUpdateUseTheExistingColumns() {
        assertNull(tokenDao.get("test-token"));
        tokenDao.create(new Row("test-token", "{\"lastPaymentId\":\"first\"}"));
        assertEquals("first", tokenDao.get("test-token").getLastPaymentId());
        tokenDao.create(new Row("test-token", "{\"lastPaymentId\":\"second\"}"));
        assertEquals("second", tokenDao.get("test-token").getLastPaymentId());
        assertEquals(1, jdbcTemplate.queryForObject("SELECT count(*) FROM ttm.token_data", Integer.class));
    }

    @Test
    void templateInsertReadAndUpdateUseTheExistingColumns() {
        assertNull(templateDao.get("test-template"));
        templateDao.create(new Row("test-template", "{}"));
        assertNotNull(templateDao.get("test-template"));
        templateDao.create(new Row("test-template", "{\"paymentsConditions\":{\"conditions\":[]}}"));
        assertTrue(templateDao.get("test-template").getPaymentsConditions().getConditions().isEmpty());
        assertEquals(1, jdbcTemplate.queryForObject("SELECT count(*) FROM ttm.template_data", Integer.class));
    }
}
