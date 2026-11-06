package dev.vality.fraudbusters.management.dao.payout.reference;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CommandDao;
import dev.vality.fraudbusters.management.domain.payout.PayoutReferenceModel;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static dev.vality.fraudbusters.management.domain.tables.PayoutReference.PAYOUT_REFERENCE;

@Component
public class PayoutReferenceDao extends AbstractDao implements CommandDao<PayoutReferenceModel> {

    public PayoutReferenceDao(DataSource dataSource) {
        super(dataSource);
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

}
