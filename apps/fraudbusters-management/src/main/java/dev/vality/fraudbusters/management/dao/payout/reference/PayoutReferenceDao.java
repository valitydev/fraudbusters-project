package dev.vality.fraudbusters.management.dao.payout.reference;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.payout.PayoutReferenceModel;
import dev.vality.fraudbusters.management.utils.PayoutScope;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.Condition;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

import static dev.vality.fraudbusters.management.domain.tables.PayoutReference.PAYOUT_REFERENCE;

@Component
public class PayoutReferenceDao extends AbstractDao implements CommandDao<PayoutReferenceModel> {

    private final RowMapper<PayoutReferenceModel> referenceRowMapper;

    public PayoutReferenceDao(DataSource dataSource) {
        super(dataSource);
        referenceRowMapper = new RecordRowMapper<>(PAYOUT_REFERENCE, PayoutReferenceModel.class);
    }

    @Override
    public void insert(PayoutReferenceModel referenceModel) {
        execute(getDslContext()
                .insertInto(PAYOUT_REFERENCE)
                .set(PAYOUT_REFERENCE.PARTY_ID, referenceModel.getPartyId())
                .set(PAYOUT_REFERENCE.WALLET_ID, referenceModel.getWalletId())
                .set(PAYOUT_REFERENCE.IS_GLOBAL, referenceModel.getIsGlobal())
                .set(PAYOUT_REFERENCE.TEMPLATE_ID, referenceModel.getTemplateId())
                .onConflict(PAYOUT_REFERENCE.PARTY_ID, PAYOUT_REFERENCE.WALLET_ID, PAYOUT_REFERENCE.IS_GLOBAL)
                .doUpdate()
                .set(PAYOUT_REFERENCE.TEMPLATE_ID, referenceModel.getTemplateId()));
    }

    @Override
    public void remove(PayoutReferenceModel referenceModel) {
        execute(getDslContext()
                .delete(PAYOUT_REFERENCE)
                .where(PAYOUT_REFERENCE.PARTY_ID.eq(referenceModel.getPartyId())
                        .and(PAYOUT_REFERENCE.WALLET_ID.eq(referenceModel.getWalletId()))
                        .and(PAYOUT_REFERENCE.IS_GLOBAL.eq(referenceModel.getIsGlobal()))));
    }

    public PayoutReferenceModel getByScope(String partyId, String walletId, boolean isGlobal) {
        return fetchOne(getDslContext()
                .selectFrom(PAYOUT_REFERENCE)
                .where(PAYOUT_REFERENCE.PARTY_ID.eq(partyId)
                        .and(PAYOUT_REFERENCE.WALLET_ID.eq(walletId))
                        .and(PAYOUT_REFERENCE.IS_GLOBAL.eq(isGlobal))), referenceRowMapper);
    }

    public List<PayoutReferenceModel> filter(String searchPattern, Boolean isGlobal, Integer size) {
        return fetch(getDslContext()
                .selectFrom(PAYOUT_REFERENCE)
                .where(filterCondition(searchPattern, isGlobal))
                .orderBy(PAYOUT_REFERENCE.IS_GLOBAL.desc(), PAYOUT_REFERENCE.PARTY_ID, PAYOUT_REFERENCE.WALLET_ID)
                .limit(PayoutScope.pageSize(size)), referenceRowMapper);
    }

    public Integer countFilter(String searchPattern, Boolean isGlobal) {
        return fetchOne(getDslContext()
                .selectCount()
                .from(PAYOUT_REFERENCE)
                .where(filterCondition(searchPattern, isGlobal)), Integer.class);
    }

    private Condition filterCondition(String searchPattern, Boolean isGlobal) {
        Condition condition = isGlobal == null ? DSL.noCondition() : PAYOUT_REFERENCE.IS_GLOBAL.eq(isGlobal);
        if (!StringUtils.hasText(searchPattern)) {
            return condition;
        }
        return condition.and(PAYOUT_REFERENCE.PARTY_ID.like(searchPattern)
                .or(PAYOUT_REFERENCE.WALLET_ID.like(searchPattern))
                .or(PAYOUT_REFERENCE.TEMPLATE_ID.like(searchPattern)));
    }

}
