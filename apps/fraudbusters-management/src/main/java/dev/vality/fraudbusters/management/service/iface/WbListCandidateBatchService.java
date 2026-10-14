package dev.vality.fraudbusters.management.service.iface;

import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;

public interface WbListCandidateBatchService {

    void save(String id, String source);

    FilterResponse<WbListCandidateBatchModel> getList(FilterRequest filter);
}
