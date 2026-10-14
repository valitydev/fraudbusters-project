package dev.vality.fraudbusters.management.dao.payment.candidate;

import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidateBatch;

import java.util.List;

public interface WbListCandidateBatchDao {

    void save(WbListCandidateBatch candidateBatch);

    List<WbListCandidateBatchModel> getList(FilterRequest filter);

}
