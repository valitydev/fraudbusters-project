package dev.vality.fraudbusters.management.dao.payout.group;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.payout.PayoutGroupReferenceModel;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Condition;
import org.jooq.SortOrder;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import static dev.vality.fraudbusters.management.domain.tables.PayoutGroupReference.PAYOUT_GROUP_REFERENCE;

@Component
public class PayoutGroupReferenceDao extends AbstractDao implements CommandDao<PayoutGroupReferenceModel> {

    private final RowMapper<PayoutGroupReferenceModel> referenceRowMapper;

    public PayoutGroupReferenceDao(DataSource dataSource) {
        super(dataSource);
        referenceRowMapper = new RecordRowMapper<>(PAYOUT_GROUP_REFERENCE, PayoutGroupReferenceModel.class);
    }

    @Override
    public void insert(PayoutGroupReferenceModel referenceModel) {
        execute(getDslContext()
                .insertInto(PAYOUT_GROUP_REFERENCE)
                .set(PAYOUT_GROUP_REFERENCE.PARTY_ID, referenceModel.getPartyId())
                .set(PAYOUT_GROUP_REFERENCE.WALLET_ID, referenceModel.getWalletId())
                .set(PAYOUT_GROUP_REFERENCE.GROUP_ID, referenceModel.getGroupId())
                .set(PAYOUT_GROUP_REFERENCE.MODIFIED_BY_USER, referenceModel.getModifiedByUser())
                .onConflict(PAYOUT_GROUP_REFERENCE.PARTY_ID, PAYOUT_GROUP_REFERENCE.WALLET_ID)
                .doUpdate()
                .set(PAYOUT_GROUP_REFERENCE.GROUP_ID, referenceModel.getGroupId())
                .set(PAYOUT_GROUP_REFERENCE.MODIFIED_BY_USER, referenceModel.getModifiedByUser())
                .set(PAYOUT_GROUP_REFERENCE.LAST_UPDATE_DATE, LocalDateTime.now(ZoneOffset.UTC)));
    }

    @Override
    public void remove(PayoutGroupReferenceModel referenceModel) {
        execute(getDslContext()
                .delete(PAYOUT_GROUP_REFERENCE)
                .where(PAYOUT_GROUP_REFERENCE.PARTY_ID.eq(referenceModel.getPartyId())
                        .and(PAYOUT_GROUP_REFERENCE.WALLET_ID.eq(referenceModel.getWalletId()))));
    }

    /**
     * Pages continue after {@code lastId}, as the payment group references do.
     */
    public List<PayoutGroupReferenceModel> filter(String searchPattern, String lastId, SortOrder sortOrder,
                                                  Integer size) {
        var where = getDslContext()
                .selectFrom(PAYOUT_GROUP_REFERENCE)
                .where(filterCondition(searchPattern));
        var sorted = addSortCondition(PAYOUT_GROUP_REFERENCE.ID, sortOrder, where);
        return fetch(addSeekIfNeed(lastId, PayoutScope.pageSize(size), sorted), referenceRowMapper);
    }

    public Integer countFilter(String searchPattern) {
        return fetchOne(getDslContext()
                .selectCount()
                .from(PAYOUT_GROUP_REFERENCE)
                .where(filterCondition(searchPattern)), Integer.class);
    }

    private Condition filterCondition(String searchPattern) {
        if (!StringUtils.hasText(searchPattern)) {
            return DSL.noCondition();
        }
        return PAYOUT_GROUP_REFERENCE.PARTY_ID.like(searchPattern)
                .or(PAYOUT_GROUP_REFERENCE.WALLET_ID.like(searchPattern))
                .or(PAYOUT_GROUP_REFERENCE.GROUP_ID.like(searchPattern));
    }

}
