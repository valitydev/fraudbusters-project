package dev.vality.fraudbusters.management.dao.payment.candidate;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.dao.payment.candidate.mapper.WbListCandidateBatchRowMapper;
import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidateBatch;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.util.List;

import static dev.vality.fraudbusters.management.dao.payment.candidate.mapper.WbListCandidateBatchRowMapper.FIELDS_FIELD_NANE;
import static dev.vality.fraudbusters.management.dao.payment.candidate.mapper.WbListCandidateBatchRowMapper.SIZE_FIELD_NANE;
import static dev.vality.fraudbusters.management.domain.tables.WbListCandidate.WB_LIST_CANDIDATE;
import static dev.vality.fraudbusters.management.domain.tables.WbListCandidateBatch.WB_LIST_CANDIDATE_BATCH;
import static org.jooq.impl.DSL.arrayAggDistinct;

@Component
public class WbListCandidateBatchDaoImpl extends AbstractDao implements WbListCandidateBatchDao {

    private final WbListCandidateBatchRowMapper wbListCandidateBatchRowMapper;


    public WbListCandidateBatchDaoImpl(DataSource dataSource) {
        super(dataSource);
        wbListCandidateBatchRowMapper = new WbListCandidateBatchRowMapper();
    }


    @Override
    public void save(WbListCandidateBatch candidateBatch) {
        Query query = getDslContext().insertInto(WB_LIST_CANDIDATE_BATCH)
                .set(getDslContext().newRecord(WB_LIST_CANDIDATE_BATCH, candidateBatch))
                .onConflict(WB_LIST_CANDIDATE_BATCH.ID)
                .doNothing();
        execute(query);
    }

    @Override
    public List<WbListCandidateBatchModel> getList(FilterRequest filter) {
        SelectOnConditionStep<Record> select = getDslContext()
                .select(WB_LIST_CANDIDATE_BATCH.fields())
                .select(DSL.count(WB_LIST_CANDIDATE.ID).as(SIZE_FIELD_NANE))
                .select(arrayAggDistinct(WB_LIST_CANDIDATE.LIST_NAME).as(FIELDS_FIELD_NANE))
                .from(WB_LIST_CANDIDATE_BATCH)
                .leftJoin(WB_LIST_CANDIDATE)
                .on(WB_LIST_CANDIDATE_BATCH.ID.eq(WB_LIST_CANDIDATE.BATCH_ID));
        Field sortField = StringUtils.hasLength(filter.getSortBy())
                ? WB_LIST_CANDIDATE_BATCH.field(filter.getSortBy())
                : WB_LIST_CANDIDATE_BATCH.INSERT_TIME;
        SelectConditionStep<Record> whereQuery = StringUtils.hasLength(filter.getSearchValue())
                ? select.where(WB_LIST_CANDIDATE_BATCH.SOURCE.like(filter.getSearchValue()))
                : select.where(DSL.trueCondition());
        SelectHavingStep<Record> groupBy = whereQuery.groupBy(WB_LIST_CANDIDATE_BATCH.ID);
        SelectSeekStep2<Record, Object, String> candidateBatchRecords =
                addSortCondition(WB_LIST_CANDIDATE_BATCH.ID, sortField, filter.getSortOrder(), groupBy);
        List<WbListCandidateBatchModel> fetchResult = fetch(addSeekIfNeed(
                        filter.getLastId(),
                        filter.getSortFieldValue(),
                        filter.getSize(),
                        candidateBatchRecords),
                wbListCandidateBatchRowMapper);
        return fetchResult != null ? fetchResult : List.of();
    }
}
