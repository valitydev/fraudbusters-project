package dev.vality.fraudbusters.management.service;

import dev.vality.fraudbusters.management.dao.payment.candidate.WbListCandidateBatchDao;
import dev.vality.fraudbusters.management.domain.WbListCandidateBatchModel;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidateBatch;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WbListCandidateBatchServiceImpl implements WbListCandidateBatchService {

    private final WbListCandidateBatchDao wbListCandidateBatchDao;

    @Transactional
    @Override
    public void save(String id, String source) {
        WbListCandidateBatch batch = new WbListCandidateBatch();
        batch.setId(id);
        batch.setSource(source);
        wbListCandidateBatchDao.save(batch);
    }

    @Transactional(readOnly = true)
    @Override
    public FilterResponse<WbListCandidateBatchModel> getList(FilterRequest filter) {
        List<WbListCandidateBatchModel> batches = wbListCandidateBatchDao.getList(filter);
        batches.forEach(wbListCandidateBatchModel -> {
            String fields = wbListCandidateBatchModel.getFields();
            wbListCandidateBatchModel.setFields(removeBrackets(fields));
        });
        FilterResponse<WbListCandidateBatchModel> response = new FilterResponse<>();
        response.setResult(batches);
        String lastId = buildLastId(filter.getSize(), batches);
        response.setLastId(lastId);
        return response;
    }

    private String buildLastId(Integer filterSize, List<WbListCandidateBatchModel> candidates) {
        if (candidates.size() == filterSize) {
            var lastCandidate = candidates.get(candidates.size() - 1);
            return lastCandidate.getId();
        }
        return null;
    }

    private String removeBrackets(String fields) {
        return fields.subSequence(1, fields.length() - 1).toString();
    }
}
