package dev.vality.fraudbusters.warehouse.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class QueryRepository {

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public List<Map<String, Object>> query(String statement, Map<String, String> params) {
        return namedParameterJdbcTemplate.queryForList(statement, params);
    }

}
