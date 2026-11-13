package dev.vality.fraudbusters.management.dao.payout.wblist;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CdDao;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutListRecord;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Condition;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

import static dev.vality.fraudbusters.management.domain.tables.PayoutListRecord.PAYOUT_LIST_RECORD;

@Component
public class PayoutListDao extends AbstractDao implements CdDao<PayoutListRecord> {

    private final RowMapper<PayoutListRecord> recordRowMapper;

    public PayoutListDao(DataSource dataSource) {
        super(dataSource);
        recordRowMapper = new RecordRowMapper<>(PAYOUT_LIST_RECORD, PayoutListRecord.class);
    }

    @Override
    public void saveListRecord(PayoutListRecord listRecord) {
        var record = getDslContext().newRecord(PAYOUT_LIST_RECORD, listRecord);
        record.changed(PAYOUT_LIST_RECORD.ID, false);
        record.changed(PAYOUT_LIST_RECORD.INSERT_TIME, false);
        execute(getDslContext()
                .insertInto(PAYOUT_LIST_RECORD)
                .set(record)
                .onConflict(PAYOUT_LIST_RECORD.PARTY_ID, PAYOUT_LIST_RECORD.WALLET_ID, PAYOUT_LIST_RECORD.LIST_TYPE,
                        PAYOUT_LIST_RECORD.LIST_NAME, PAYOUT_LIST_RECORD.VALUE)
                .doUpdate()
                .set(PAYOUT_LIST_RECORD.ROW_INFO, listRecord.getRowInfo()));
    }

    @Override
    public void removeRecord(PayoutListRecord listRecord) {
        execute(getDslContext()
                .delete(PAYOUT_LIST_RECORD)
                .where(buildKeyCondition(listRecord)));
    }

    private Condition buildKeyCondition(PayoutListRecord listRecord) {
        return PAYOUT_LIST_RECORD.PARTY_ID.eq(listRecord.getPartyId())
                .and(PAYOUT_LIST_RECORD.WALLET_ID.eq(listRecord.getWalletId()))
                .and(PAYOUT_LIST_RECORD.LIST_TYPE.eq(listRecord.getListType()))
                .and(PAYOUT_LIST_RECORD.LIST_NAME.eq(listRecord.getListName()))
                .and(PAYOUT_LIST_RECORD.VALUE.eq(listRecord.getValue()));
    }

    public PayoutListRecord findById(String id) {
        return fetchOne(getDslContext()
                .selectFrom(PAYOUT_LIST_RECORD)
                .where(PAYOUT_LIST_RECORD.ID.eq(id)), recordRowMapper);
    }

    /**
     * Pages continue after {@code lastId}, as the payment lists do.
     */
    public List<PayoutListRecord> filter(ListType listType, List<String> listNames, String searchPattern,
                                         String lastId, SortOrder sortOrder, Integer size) {
        var where = getDslContext()
                .selectFrom(PAYOUT_LIST_RECORD)
                .where(filterCondition(listType, listNames, searchPattern));
        var sorted = addSortCondition(PAYOUT_LIST_RECORD.ID, sortOrder, where);
        return fetch(addSeekIfNeed(lastId, PayoutScope.pageSize(size), sorted), recordRowMapper);
    }

    public Integer countFilter(ListType listType, List<String> listNames, String searchPattern) {
        return fetchOne(getDslContext()
                .selectCount()
                .from(PAYOUT_LIST_RECORD)
                .where(filterCondition(listType, listNames, searchPattern)), Integer.class);
    }

    public List<String> getCurrentListNames(ListType listType) {
        return fetch(getDslContext()
                .selectDistinct(PAYOUT_LIST_RECORD.LIST_NAME)
                .from(PAYOUT_LIST_RECORD)
                .where(PAYOUT_LIST_RECORD.LIST_TYPE.eq(listType))
                .orderBy(PAYOUT_LIST_RECORD.LIST_NAME),
                (resultSet, i) -> resultSet.getString(PAYOUT_LIST_RECORD.LIST_NAME.getName()));
    }

    private Condition filterCondition(ListType listType, List<String> listNames, String searchPattern) {
        Condition condition = PAYOUT_LIST_RECORD.LIST_TYPE.eq(listType);
        if (!CollectionUtils.isEmpty(listNames)) {
            condition = condition.and(PAYOUT_LIST_RECORD.LIST_NAME.in(listNames));
        }
        if (StringUtils.hasText(searchPattern)) {
            condition = condition.and(PAYOUT_LIST_RECORD.VALUE.like(searchPattern)
                    .or(PAYOUT_LIST_RECORD.PARTY_ID.like(searchPattern))
                    .or(PAYOUT_LIST_RECORD.WALLET_ID.like(searchPattern)));
        }
        return condition;
    }

}
