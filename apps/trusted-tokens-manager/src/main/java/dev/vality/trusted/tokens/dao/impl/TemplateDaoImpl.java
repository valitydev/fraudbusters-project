package dev.vality.trusted.tokens.dao.impl;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.dao.impl.AbstractGenericDao;
import dev.vality.mapper.RecordRowMapper;
import dev.vality.trusted.tokens.ConditionTemplate;
import dev.vality.trusted.tokens.dao.TemplateDao;
import dev.vality.trusted.tokens.domain.tables.records.TemplateDataRecord;
import dev.vality.trusted.tokens.model.Row;
import lombok.SneakyThrows;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Map;

import static dev.vality.trusted.tokens.domain.tables.TemplateData.TEMPLATE_DATA;


@Component
public class TemplateDaoImpl extends AbstractGenericDao implements TemplateDao {

    private final RowMapper<Row> listRecordRowMapper;
    private final JsonMapper objectMapper;

    public TemplateDaoImpl(DataSource dataSource, JsonMapper objectMapper) {
        super(dataSource);
        this.objectMapper = objectMapper;
        listRecordRowMapper = new RecordRowMapper<>(TEMPLATE_DATA, Row.class);
    }

    @Override
    public void create(Row row) {
        Query query = getDslContext().insertInto(TEMPLATE_DATA)
                .set(getDslContext().newRecord(TEMPLATE_DATA, row))
                .onConflict(TEMPLATE_DATA.TEMPLATE_NAME)
                .doUpdate().set(Map.of(TEMPLATE_DATA.TEMPLATE_CONDITION, row.getValue()));
        execute(query);
    }

    @SneakyThrows
    @Override
    public ConditionTemplate get(String key) {
        SelectConditionStep<TemplateDataRecord> where = getDslContext()
                .selectFrom(TEMPLATE_DATA)
                .where(TEMPLATE_DATA.TEMPLATE_NAME.eq(key));
        var data = fetchOne(where, listRecordRowMapper);
        return data != null
                ? objectMapper.readValue(data.getValue(), ConditionTemplate.class)
                : null;
    }

}
