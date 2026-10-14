package dev.vality.trusted.tokens.dao;


import dev.vality.dao.GenericDao;
import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.model.Row;

public interface TemplateDao extends GenericDao {

    void create(Row row);

    ConditionTemplate get(String key);
}
