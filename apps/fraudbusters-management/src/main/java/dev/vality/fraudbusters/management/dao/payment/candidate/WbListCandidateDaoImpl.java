package dev.vality.fraudbusters.management.dao.payment.candidate;

import dev.vality.fraudbusters.management.dao.AbstractDao;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.domain.tables.records.WbListCandidateRecord;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.*;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;

import static dev.vality.fraudbusters.management.domain.tables.WbListCandidate.WB_LIST_CANDIDATE;

@Component
public class WbListCandidateDaoImpl extends AbstractDao implements WbListCandidateDao {

    private final RowMapper<WbListCandidate> candidateRowMapper;

    public WbListCandidateDaoImpl(DataSource dataSource) {
        super(dataSource);
        candidateRowMapper = new RecordRowMapper<>(WB_LIST_CANDIDATE, WbListCandidate.class);
    }

    @Override
    public void save(WbListCandidate candidate) {
        Query query = getDslContext().insertInto(WB_LIST_CANDIDATE)
                .set(getDslContext().newRecord(WB_LIST_CANDIDATE, candidate))
                .onConflict(WB_LIST_CANDIDATE.ID)
                .doUpdate()
                .set(getDslContext().newRecord(WB_LIST_CANDIDATE, candidate));
        execute(query);
    }

    @Override
    public List<WbListCandidate> getList(FilterRequest filter) {
        SelectWhereStep<WbListCandidateRecord> from = getDslContext()
                .selectFrom(WB_LIST_CANDIDATE);
        Field<String> sortField = StringUtils.hasLength(filter.getSortBy())
                ? WB_LIST_CANDIDATE.field(filter.getSortBy(), String.class)
                : WB_LIST_CANDIDATE.LIST_NAME;

        SelectConditionStep<WbListCandidateRecord> defaultWhere =
                from.where(WB_LIST_CANDIDATE.APPROVED.eq(Boolean.FALSE));
        SelectConditionStep<WbListCandidateRecord> whereQuery = StringUtils.hasLength(filter.getSearchValue())
                ? defaultWhere.and(WB_LIST_CANDIDATE.VALUE.like(filter.getSearchValue())
                .or(WB_LIST_CANDIDATE.LIST_NAME.like(filter.getSearchValue()))
                .or(WB_LIST_CANDIDATE.BATCH_ID.like(filter.getSearchValue())))
                : from.where(DSL.trueCondition());
        SelectSeekStep2<WbListCandidateRecord, String, Long> candidateRecords = addSortCondition(WB_LIST_CANDIDATE.ID,
                sortField, filter.getSortOrder(), whereQuery);
        return fetch(addSeekIfNeed(
                        StringUtils.hasLength(filter.getLastId()) ? Long.valueOf(filter.getLastId()) : null,
                        filter.getSortFieldValue(),
                        filter.getSize(),
                        candidateRecords),
                candidateRowMapper);
    }

    @Override
    public void approve(List<Long> ids) {
        Query query = getDslContext().update(WB_LIST_CANDIDATE)
                .set(WB_LIST_CANDIDATE.APPROVED, Boolean.TRUE)
                .set(WB_LIST_CANDIDATE.UPDATE_TIME, LocalDateTime.now())
                .where(WB_LIST_CANDIDATE.ID.in(ids));
        execute(query);
    }

    @Override
    public List<WbListCandidate> getByIds(List<Long> ids) {
        SelectConditionStep<WbListCandidateRecord> where = getDslContext()
                .selectFrom(WB_LIST_CANDIDATE)
                .where(WB_LIST_CANDIDATE.ID.in(ids))
                .and(WB_LIST_CANDIDATE.APPROVED.eq(Boolean.FALSE));
        return fetch(where, candidateRowMapper);
    }

}
