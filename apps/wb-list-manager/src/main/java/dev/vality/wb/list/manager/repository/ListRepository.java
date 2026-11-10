package dev.vality.wb.list.manager.repository;

import dev.vality.wb.list.manager.exception.DbExecutionException;
import dev.vality.wb.list.manager.model.Row;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

/** Rows of one list table; the table name comes from configuration, never from a request. */
@Slf4j
public class ListRepository implements CrudRepository<Row, String> {

    private final JdbcTemplate jdbcTemplate;
    private final String upsertSql;
    private final String deleteSql;
    private final String selectSql;

    public ListRepository(JdbcTemplate jdbcTemplate, String table) {
        this.jdbcTemplate = jdbcTemplate;
        this.upsertSql = "INSERT INTO " + table + "(id, value) VALUES(?,?) ON CONFLICT (id) DO UPDATE SET value = ?";
        this.deleteSql = "DELETE FROM " + table + " WHERE id = ?";
        this.selectSql = "SELECT value FROM " + table + " WHERE id = ?";
    }

    @Override
    public void create(Row row) {
        try {
            log.info("ListRepository create in row: {}", row);
            jdbcTemplate.update(upsertSql, row.getKey(), row.getValue(), row.getValue());
        } catch (Exception e) {
            log.error("Exception in ListRepository when create e: ", e);
            throw new DbExecutionException();
        }
    }

    @Override
    public void remove(Row row) {
        try {
            log.info("ListRepository remove from row: {}", row);
            jdbcTemplate.update(deleteSql, row.getKey());
        } catch (Exception e) {
            log.error("Exception in ListRepository when remove e: ", e);
            throw new DbExecutionException(e);
        }
    }

    @Override
    public Optional<Row> get(String key) {
        try {
            log.info("ListRepository select from row by key: {}", key);
            String value = jdbcTemplate.queryForObject(selectSql, new Object[] {key}, String.class);
            return value != null
                    ? Optional.of(new Row(key, value))
                    : Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Exception in ListRepository when get e: ", e);
            throw new DbExecutionException(e);
        }
    }

}
