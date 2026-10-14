package dev.vality.fraudbusters.management.dao.payment.candidate.mapper;

import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;

import static dev.vality.fraudbusters.management.domain.Tables.WB_LIST_CANDIDATE_BATCH;

public class WbListCandidateBatchRowMapper implements RowMapper<WbListCandidateBatchModel> {

    public static final String SIZE_FIELD_NANE = "size";
    public static final String FIELDS_FIELD_NANE = "fields";

    @Override
    public WbListCandidateBatchModel mapRow(ResultSet resultSet, int i) throws SQLException {
        return WbListCandidateBatchModel.builder()
                .id(resultSet.getString(WB_LIST_CANDIDATE_BATCH.ID.getName()))
                .source(resultSet.getString(WB_LIST_CANDIDATE_BATCH.SOURCE.getName()))
                .insertTime(resultSet.getObject(WB_LIST_CANDIDATE_BATCH.INSERT_TIME.getName(),
                        LocalDateTime.class))
                .size(resultSet.getInt(SIZE_FIELD_NANE))
                .fields(resultSet.getString(FIELDS_FIELD_NANE))
                .build();
    }

}
