package dev.vality.wb.list.manager.repository;

import dev.vality.wb.list.manager.exception.DbExecutionException;
import dev.vality.wb.list.manager.model.Row;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ListRepository implements CrudRepository<Row, String> {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void create(Row row) {
        try {
            log.info("ListRepository create in row: {}", row);
            jdbcTemplate.update("""
                    INSERT INTO wb_list.row(id, value) VALUES(?,?) ON CONFLICT (id) DO UPDATE
                    SET value = ?
                    """, row.getKey(), row.getValue(), row.getValue());
        } catch (Exception e) {
            log.error("Exception in ListRepository when create e: ", e);
            throw new DbExecutionException();
        }
    }

    @Override
    public void remove(Row row) {
        try {
            log.info("ListRepository remove from row: {}", row);
            jdbcTemplate.update("DELETE FROM wb_list.row WHERE id = ?", row.getKey());
        } catch (Exception e) {
            log.error("Exception in ListRepository when remove e: ", e);
            throw new DbExecutionException(e);
        }
    }

    @Override
    public Optional<Row> get(String key) {
        try {
            log.info("ListRepository select from row by key: {}", key);
            String value = jdbcTemplate.queryForObject("SELECT value FROM wb_list.row WHERE id = ?",
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
