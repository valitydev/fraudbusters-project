package dev.vality.fraudbusters.management.dao.payout;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.CdDao;
import dev.vality.fraudbusters.management.domain.tables.pojos.PayoutListRecord;
import org.jooq.Condition;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static dev.vality.fraudbusters.management.domain.tables.PayoutListRecord.PAYOUT_LIST_RECORD;

@Component
public class PayoutListDao extends AbstractDao implements CdDao<PayoutListRecord> {

    public PayoutListDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public void saveListRecord(PayoutListRecord listRecord) {
        execute(getDslContext()
                .insertInto(PAYOUT_LIST_RECORD)
                .set(getDslContext().newRecord(PAYOUT_LIST_RECORD, listRecord))
                .onConflict(PAYOUT_LIST_RECORD.PARTY_ID, PAYOUT_LIST_RECORD.WALLET_ID, PAYOUT_LIST_RECORD.LIST_TYPE,
                        PAYOUT_LIST_RECORD.LIST_NAME, PAYOUT_LIST_RECORD.VALUE)
                .doUpdate()
                .set(PAYOUT_LIST_RECORD.ROW_INFO, listRecord.getRowInfo()));
    }

    @Override
    public void removeRecord(PayoutListRecord listRecord) {
        execute(getDslContext()
                .delete(PAYOUT_LIST_RECORD)
                .where(byKey(listRecord)));
    }

    private Condition byKey(PayoutListRecord listRecord) {
        return PAYOUT_LIST_RECORD.PARTY_ID.eq(listRecord.getPartyId())
                .and(PAYOUT_LIST_RECORD.WALLET_ID.eq(listRecord.getWalletId()))
                .and(PAYOUT_LIST_RECORD.LIST_TYPE.eq(listRecord.getListType()))
                .and(PAYOUT_LIST_RECORD.LIST_NAME.eq(listRecord.getListName()))
                .and(PAYOUT_LIST_RECORD.VALUE.eq(listRecord.getValue()));
    }

}
