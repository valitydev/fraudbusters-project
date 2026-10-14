package dev.vality.fraudbusters.notificator.dao;

import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ReportRecord;
import dev.vality.mapper.RecordRowMapper;
import org.jooq.DSLContext;
import org.jooq.Query;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.REPORT;

@Component
public class ReportNotificationDaoImpl extends AbstractDao implements ReportNotificationDao {

    private final RowMapper<Report> listRecordRowMapper;

    public ReportNotificationDaoImpl(DataSource dataSource) {
        super(dataSource);
        listRecordRowMapper = new RecordRowMapper<>(REPORT, Report.class);
    }

    @Override
    public Optional<Long> insert(Report report) {
        Query query = getDslContext()
                .insertInto(REPORT)
                .set(getDslContext().newRecord(REPORT, report))
                .onConflict(REPORT.ID)
                .doUpdate()
                .set(getDslContext().newRecord(REPORT, report))
                .returning(REPORT.ID);
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        executeOne(query, keyHolder);
        return Optional.ofNullable(keyHolder.getKey()).map(Number::longValue);
    }

    @Override
    public Report getLastSendById(Long id) {
        DSLContext dslContext = getDslContext();
        SelectConditionStep<ReportRecord> where = dslContext
                .selectFrom(REPORT)
                .where(REPORT.ID.eq(
                                dslContext.select(DSL.max(REPORT.ID))
                                        .from(REPORT)
                                        .where(REPORT.NOTIFICATION_ID.eq(id)
                                                .and(REPORT.STATUS.eq(ReportStatus.send))
                                        )
                        )
                );
        return fetchOne(where, listRecordRowMapper);
    }

    @Override
    public List<Report> getReportsByStatusAndFromTime(ReportStatus status, LocalDateTime from) {
        DSLContext dslContext = getDslContext();
        SelectConditionStep<ReportRecord> where = dslContext
                .selectFrom(REPORT)
                .where(REPORT.ID.in(dslContext.select(DSL.max(REPORT.ID))
                        .from(REPORT)
                        .where(REPORT.STATUS.eq(status)
                                .and(REPORT.CREATED_AT.lt(from)))));
        return fetch(where, listRecordRowMapper);
    }

}
