package dev.vality.trusted.tokens.dao;


import dev.vality.dao.GenericDao;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.Row;

public interface TokenDao extends GenericDao {

    void create(Row row);

    CardTokenData get(String key);
}
