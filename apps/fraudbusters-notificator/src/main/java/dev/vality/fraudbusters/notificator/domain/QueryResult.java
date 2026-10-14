package dev.vality.fraudbusters.notificator.domain;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class QueryResult {

    List<Map<String, String>> results;

}
