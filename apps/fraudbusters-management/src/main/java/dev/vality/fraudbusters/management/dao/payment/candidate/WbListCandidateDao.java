package dev.vality.fraudbusters.management.dao.payment.candidate;

import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;

import java.util.List;

public interface WbListCandidateDao {

    void save(WbListCandidate candidate);

    List<WbListCandidate> getList(FilterRequest filter);

    void approve(List<Long> ids);

    List<WbListCandidate> getByIds(List<Long> ids);

}
