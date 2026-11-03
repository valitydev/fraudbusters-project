package dev.vality.wb.list.manager.repository;

import dev.vality.wb.list.manager.exception.DbExecutionException;
import dev.vality.wb.list.manager.model.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

/** Rows of one list table; the table name comes from configuration, never from a request. */
@Slf4j
@RequiredArgsConstructor
public class ListRepository implements CrudRepository<Row, String> {

    private final JdbcTemplate jdbcTemplate;
    private final String table;

    @Override
    public void create(Row row) {
        try {
            log.debug("Creating list row in {}", table);
            jdbcTemplate.update("""
                    INSERT INTO %s(id, value) VALUES(?,?) ON CONFLICT (id) DO UPDATE
                    SET value = ?
                    """.formatted(table), row.getKey(), row.getValue(), row.getValue());
        } catch (Exception e) {
            log.error("Exception in ListRepository when create e: ", e);
            throw new DbExecutionException();
        }
    }

    @Override
    public void remove(Row row) {
        try {
            log.debug("Removing list row from {}", table);
            jdbcTemplate.update("DELETE FROM " + table + " WHERE id = ?", row.getKey());
        } catch (Exception e) {
            log.error("Exception in ListRepository when remove e: ", e);
            throw new DbExecutionException(e);
        }
    }

    @Override
    public Optional<Row> get(String key) {
        try {
            log.debug("Reading list row from {}", table);
            String value = jdbcTemplate.queryForObject("SELECT value FROM " + table + " WHERE id = ?",
                    new Object[] {key}, String.class);
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
