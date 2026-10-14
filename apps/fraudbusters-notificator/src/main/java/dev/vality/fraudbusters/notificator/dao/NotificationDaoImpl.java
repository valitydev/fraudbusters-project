package dev.vality.fraudbusters.notificator.dao;

import dev.vality.fraudbusters.notificator.dao.domain.enums.NotificationStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationRecord;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION;

@Component
public class NotificationDaoImpl extends AbstractDao implements NotificationDao {

    private final RowMapper<Notification> listRecordRowMapper;

    public NotificationDaoImpl(DataSource dataSource) {
        super(dataSource);
        listRecordRowMapper = new RecordRowMapper<>(NOTIFICATION, Notification.class);
    }

    @Override
    public Notification insert(Notification notification) {
        Query query = getDslContext()
                .insertInto(NOTIFICATION)
                .set(getDslContext().newRecord(NOTIFICATION, notification))
                .onConflict(NOTIFICATION.ID)
                .doUpdate()
                .set(getDslContext().newRecord(NOTIFICATION, notification))
                .returning(NOTIFICATION.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        execute(query, keyHolder);
        notification.setId(keyHolder.getKey().longValue());
        return notification;
    }

    @Override
    public void remove(Long id) {
        DeleteConditionStep<NotificationRecord> where = getDslContext()
                .delete(NOTIFICATION)
                .where(NOTIFICATION.ID.eq(id));
        execute(where);
    }

    @Override
    public Notification getById(Long id) {
        SelectConditionStep<NotificationRecord> where = getDslContext()
                .selectFrom(NOTIFICATION)
                .where(NOTIFICATION.ID.eq(id));
        return fetchOne(where, listRecordRowMapper);
    }

    @Override
    public List<Notification> getByStatus(NotificationStatus status) {
        SelectConditionStep<NotificationRecord> where = getDslContext()
                .selectFrom(NOTIFICATION)
                .where(NOTIFICATION.STATUS.eq(status));
        return fetch(where, listRecordRowMapper);
    }

    @Override
    public List<Notification> getAll(FilterDto filter) {
        String likeExpression = SqlFilterUtils.prepareSearchField(filter.getSearchFiled());
        SelectWhereStep<NotificationRecord> select = getDslContext()
                .selectFrom(NOTIFICATION);
        SelectConnectByStep<NotificationRecord> where =
                StringUtils.hasLength(likeExpression)
                        ? select.where(NOTIFICATION.NAME.likeIgnoreCase(likeExpression)
                        .or(NOTIFICATION.SUBJECT.likeIgnoreCase(likeExpression))
                        .or(NOTIFICATION.CHANNEL.likeIgnoreCase(likeExpression)))
                        : select
                        .where(Objects.nonNull(filter.getContinuationId())
                                ? NOTIFICATION.ID.greaterThan(filter.getContinuationId())
                                : DSL.noCondition());
        SelectForUpdateStep<NotificationRecord> query =
                where.orderBy(NOTIFICATION.ID)
                        .limit(filter.getSize());
        return fetch(query, listRecordRowMapper);
    }


}
