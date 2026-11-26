package dev.vality.fraudbusters.management.dao;

import dev.vality.fraudbusters.management.domain.enums.ListType;
import org.jooq.Field;
import org.jooq.Table;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

import static dev.vality.fraudbusters.management.domain.Tables.*;

@Repository
public class CatalogMetadataDao extends AbstractDao {

    public CatalogMetadataDao(DataSource dataSource) {
        super(dataSource);
    }

    public int countPaymentGroups() {
        return countGroups(F_GROUP, F_GROUP.GROUP_ID);
    }

    public int countPayoutGroups() {
        return countGroups(PAYOUT_GROUP, PAYOUT_GROUP.GROUP_ID);
    }

    public int countCandidateBatches() {
        return fetchOne(getDslContext().selectCount().from(WB_LIST_CANDIDATE_BATCH), Integer.class);
    }

    public List<ListNameCount> countPaymentListRows() {
        return countByName(WB_LIST_RECORDS, WB_LIST_RECORDS.LIST_TYPE, WB_LIST_RECORDS.LIST_NAME);
    }

    public List<ListNameCount> countPayoutListRows() {
        return countByName(PAYOUT_LIST_RECORD, PAYOUT_LIST_RECORD.LIST_TYPE, PAYOUT_LIST_RECORD.LIST_NAME);
    }

    private int countGroups(Table<?> table, Field<String> groupId) {
        return fetchOne(getDslContext().select(DSL.countDistinct(groupId)).from(table), Integer.class);
    }

    private List<ListNameCount> countByName(Table<?> table, Field<ListType> listType, Field<String> listName) {
        var query = getDslContext().select(listType, listName, DSL.count()).from(table)
                .groupBy(listType, listName)
                .orderBy(listName);
        return fetch(query, (rs, row) -> new ListNameCount(
                ListType.lookupLiteral(rs.getString(1)), rs.getString(2), rs.getInt(3)));
    }

    public record ListNameCount(ListType type, String name, int count) {
    }
}
