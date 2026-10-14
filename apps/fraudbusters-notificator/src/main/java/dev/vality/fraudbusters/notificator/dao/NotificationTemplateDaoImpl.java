package dev.vality.fraudbusters.notificator.dao;

import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationTemplateRecord;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.SelectConditionStep;
import org.jooq.SelectWhereStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.List;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION_TEMPLATE;

@Component
public class NotificationTemplateDaoImpl extends AbstractDao implements NotificationTemplateDao {

    private final RowMapper<NotificationTemplate> listRecordRowMapper;

    public NotificationTemplateDaoImpl(DataSource dataSource) {
        super(dataSource);
        listRecordRowMapper = new RecordRowMapper<>(NOTIFICATION_TEMPLATE, NotificationTemplate.class);
    }

    @Override
    public NotificationTemplate getById(Integer id) {
        SelectConditionStep<NotificationTemplateRecord> where = getDslContext()
                .selectFrom(NOTIFICATION_TEMPLATE)
                .where(NOTIFICATION_TEMPLATE.ID.eq(id));
        return fetchOne(where, listRecordRowMapper);
    }

    @Override
    public List<NotificationTemplate> getAll() {
        SelectWhereStep<NotificationTemplateRecord> notificationTemplateRecords = getDslContext()
                .selectFrom(NOTIFICATION_TEMPLATE);
        return fetch(notificationTemplateRecords, listRecordRowMapper);
    }
}
