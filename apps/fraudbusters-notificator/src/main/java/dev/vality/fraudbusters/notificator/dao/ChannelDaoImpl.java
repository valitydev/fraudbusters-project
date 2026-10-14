package dev.vality.fraudbusters.notificator.dao;

import dev.vality.fraudbusters.notificator.dao.domain.enums.ChannelType;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ChannelRecord;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;
import java.util.Objects;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.CHANNEL;

@Component
public class ChannelDaoImpl extends AbstractDao implements ChannelDao {

    private final RowMapper<Channel> listRecordRowMapper;

    public ChannelDaoImpl(DataSource dataSource) {
        super(dataSource);
        listRecordRowMapper = new RecordRowMapper<>(CHANNEL, Channel.class);
    }

    @Override
    public Channel insert(Channel channel) {
        Query query = getDslContext()
                .insertInto(CHANNEL)
                .set(getDslContext().newRecord(CHANNEL, channel))
                .onConflict(CHANNEL.NAME)
                .doUpdate()
                .set(getDslContext().newRecord(CHANNEL, channel));
        execute(query);
        return channel;
    }

    @Override
    public void remove(String name) {
        DeleteConditionStep<ChannelRecord> where = getDslContext()
                .delete(CHANNEL)
                .where(CHANNEL.NAME.eq(name));
        execute(where);
    }

    @Override
    public Channel getByName(String name) {
        SelectConditionStep<ChannelRecord> where = getDslContext()
                .selectFrom(CHANNEL)
                .where(CHANNEL.NAME.eq(name));
        return fetchOne(where, listRecordRowMapper);
    }

    @Override
    public List<Channel> getAll(FilterDto filter) {
        String likeExpression = SqlFilterUtils.prepareSearchField(filter.getSearchFiled());
        SelectWhereStep<ChannelRecord> select = getDslContext()
                .selectFrom(CHANNEL);
        Condition condition = DSL.noCondition();
        if (Objects.nonNull(filter.getContinuationString())) {
            condition = condition.and(CHANNEL.NAME.gt(filter.getContinuationString()));
        }
        if (StringUtils.hasLength(likeExpression)) {
            condition = condition.and(CHANNEL.NAME.likeIgnoreCase(likeExpression)
                    .or(CHANNEL.DESTINATION.likeIgnoreCase(likeExpression)));
        }
        SelectForUpdateStep<ChannelRecord> query = select
                .where(condition)
                .orderBy(CHANNEL.NAME)
                .limit(filter.getSize());
        return fetch(query, listRecordRowMapper);
    }

    @Override
    public List<ChannelType> getAllTypes() {
        SelectJoinStep<Record1<ChannelType>> select = getDslContext()
                .select(CHANNEL.TYPE)
                .from(CHANNEL);
        return fetch(select, (resultSet, i) -> ChannelType.valueOf(resultSet.getString(CHANNEL.TYPE.getName())));
    }

}
