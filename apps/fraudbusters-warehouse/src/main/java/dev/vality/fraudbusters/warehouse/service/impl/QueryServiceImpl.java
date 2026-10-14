package dev.vality.fraudbusters.warehouse.service.impl;

import dev.vality.fraudbusters.warehouse.repository.QueryRepository;
import dev.vality.fraudbusters.warehouse.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryServiceImpl implements QueryService {

    private final QueryRepository repository;

    @Override
    public List<Map<String, String>> query(String statement, Map<String, String> params) {
        log.info("QueryService execute statement: {}, \n params: {}", statement, params);
        List<Map<String, Object>> result = repository.query(statement, params);
        List<Map<String, String>> resultStringMap = result.stream()
                .map(map -> map.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> String.valueOf(e.getValue()))
                        )
                ).collect(Collectors.toList());
        log.info("QueryService result: {}", resultStringMap);
        return resultStringMap;
    }
}
