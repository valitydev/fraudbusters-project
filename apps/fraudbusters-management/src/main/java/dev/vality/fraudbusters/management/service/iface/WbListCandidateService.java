package dev.vality.fraudbusters.management.service.iface;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;

import java.util.List;

public interface WbListCandidateService {

    String sendToCandidate(List<FraudDataCandidate> candidates);

    void save(WbListCandidate candidate);

    FilterResponse<WbListCandidate> getList(FilterRequest filter);

    void approve(List<Long> ids, String initiator);

}
