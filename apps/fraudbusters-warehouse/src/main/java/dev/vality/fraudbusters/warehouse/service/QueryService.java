package dev.vality.fraudbusters.warehouse.service;

import java.util.List;
import java.util.Map;

public interface QueryService {

    List<Map<String, String>> query(String statement, Map<String, String> params);
}
