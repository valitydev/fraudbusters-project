package dev.vality.fraudbusters.notificator.serializer;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.json.JsonMapper;
import dev.vality.fraudbusters.notificator.domain.QueryResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryResultSerde {

    private final JsonMapper objectMapper;

    public String serialize(List<Map<String, String>> queryResult) {
        log.debug("QueryResultSerde serialize queryResult: {}", queryResult);
        try {
            QueryResult result = new QueryResult();
            result.setResults(queryResult);
            return objectMapper.writeValueAsString(result);
        } catch (JacksonException e) {
            log.error("QueryResultSerde error when serialize queryResult: {} e:", queryResult, e);
        }
        return "";
    }

    public Optional<QueryResult> deserialize(String string) {
        log.debug("QueryResultSerde deserialize string: {}", string);
        try {
            return Optional.ofNullable(objectMapper.readValue(string, QueryResult.class));
        } catch (JacksonException e) {
            log.error("QueryResultSerde error when deserialize string: {} e:", string, e);
        }
        return Optional.empty();
    }
}
