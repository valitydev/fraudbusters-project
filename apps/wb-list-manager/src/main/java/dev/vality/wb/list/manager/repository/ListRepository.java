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
            log.debug("Creating list row, payout: {}", row.isPayout());
            jdbcTemplate.update("""
                    INSERT INTO %s(id, value) VALUES(?,?) ON CONFLICT (id) DO UPDATE
                    SET value = ?
                    """.formatted(table(row.isPayout())), row.getKey(), row.getValue(), row.getValue());
        } catch (Exception e) {
            log.error("Exception in ListRepository when create e: ", e);
            throw new DbExecutionException();
        }
    }

    @Override
    public void remove(Row row) {
        try {
            log.debug("Removing list row, payout: {}", row.isPayout());
            jdbcTemplate.update("DELETE FROM " + table(row.isPayout()) + " WHERE id = ?", row.getKey());
        } catch (Exception e) {
            log.error("Exception in ListRepository when remove e: ", e);
            throw new DbExecutionException(e);
        }
    }

    @Override
    public Optional<Row> get(String key) {
        return get(key, false);
    }

    public Optional<Row> get(String key, boolean payout) {
        try {
            log.debug("Reading list row, payout: {}", payout);
            String value = jdbcTemplate.queryForObject("SELECT value FROM " + table(payout) + " WHERE id = ?",
                    new Object[] {key}, String.class);
            return value != null
                    ? Optional.of(new Row(key, value, payout))
                    : Optional.empty();
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            log.error("Exception in ListRepository when get e: ", e);
            throw new DbExecutionException(e);
        }
    }

    private String table(boolean payout) {
        return payout ? "wb_list.payout_row" : "wb_list.row";
    }
}
