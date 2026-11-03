package dev.vality.fraudbusters.management.dao.payout;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.payout.PayoutGroupReferenceModel;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static dev.vality.fraudbusters.management.domain.tables.PayoutGroupReference.PAYOUT_GROUP_REFERENCE;

@Component
public class PayoutGroupReferenceDao extends AbstractDao implements CommandDao<PayoutGroupReferenceModel> {

    public PayoutGroupReferenceDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void insert(PayoutGroupReferenceModel referenceModel) {
        execute(getDslContext()
                .insertInto(PAYOUT_GROUP_REFERENCE)
                .set(PAYOUT_GROUP_REFERENCE.PARTY_ID, referenceModel.getPartyId())
                .set(PAYOUT_GROUP_REFERENCE.WALLET_ID, referenceModel.getWalletId())
                .set(PAYOUT_GROUP_REFERENCE.GROUP_ID, referenceModel.getGroupId())
                .onConflict(PAYOUT_GROUP_REFERENCE.PARTY_ID, PAYOUT_GROUP_REFERENCE.WALLET_ID)
                .doUpdate()
                .set(PAYOUT_GROUP_REFERENCE.GROUP_ID, referenceModel.getGroupId()));
    }

    @Override
    public void remove(PayoutGroupReferenceModel referenceModel) {
        execute(getDslContext()
                .delete(PAYOUT_GROUP_REFERENCE)
                .where(PAYOUT_GROUP_REFERENCE.PARTY_ID.eq(referenceModel.getPartyId())
                        .and(PAYOUT_GROUP_REFERENCE.WALLET_ID.eq(referenceModel.getWalletId()))));
    }

}
