package dev.vality.trusted.tokens.dao.impl;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.trusted.tokens.dao.TokenDao;
import dev.vality.trusted.tokens.domain.tables.records.TokenDataRecord;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.Row;
import lombok.SneakyThrows;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

import static dev.vality.trusted.tokens.domain.tables.TokenData.TOKEN_DATA;


@Component
public class TokenDaoImpl extends AbstractGenericDao implements TokenDao {

    private final RowMapper<Row> listRecordRowMapper;
    private final JsonMapper objectMapper;

    public TokenDaoImpl(DataSource dataSource, JsonMapper objectMapper) {
        super(dataSource);
        this.objectMapper = objectMapper;
        listRecordRowMapper = (resultSet, rowNumber) -> new Row(
                resultSet.getString("token"), resultSet.getString("data"));
    }

    @Override
    public void create(Row row) {
        Query query = getDslContext().insertInto(TOKEN_DATA)
                .set(TOKEN_DATA.TOKEN, row.getKey())
                .set(TOKEN_DATA.DATA, row.getValue())
                .onConflict(TOKEN_DATA.TOKEN)
                .doUpdate().set(Map.of(TOKEN_DATA.DATA, row.getValue()));
        execute(query);
    }

    @SneakyThrows
    @Override
    public CardTokenData get(String key) {
        SelectConditionStep<TokenDataRecord> where = getDslContext()
                .selectFrom(TOKEN_DATA)
                .where(TOKEN_DATA.TOKEN.eq(key));
        var data = fetchOne(where, listRecordRowMapper);
        return data != null
                ? objectMapper.readValue(data.getValue(), CardTokenData.class)
                : null;
    }

}
