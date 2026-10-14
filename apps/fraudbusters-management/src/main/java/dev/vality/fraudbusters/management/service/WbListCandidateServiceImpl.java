package dev.vality.fraudbusters.management.service;

import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.dao.payment.candidate.WbListCandidateDao;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.response.FilterResponse;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.exception.KafkaProduceException;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.convert.converter.Converter;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Slf4j
@Service
@RequiredArgsConstructor
public class WbListCandidateServiceImpl implements WbListCandidateService {

    private final WbListCandidateDao wbListCandidateDao;
    private final KafkaTemplate<String, TBase> kafkaTemplate;
    @Value("${kafka.topic.wblist.candidate}")
    public String topicCandidate;
    private final WbListCommandService wbListCommandService;
    private final Converter<WbListCandidate, Row> rowConverter;

    @Override
    public String sendToCandidate(List<FraudDataCandidate> candidates) {
        try {
            String key = candidates.get(0).getBatchId();
            for (FraudDataCandidate candidate : candidates) {
                kafkaTemplate.send(topicCandidate, key, candidate).get();
                log.info("WbListCandidateService send candidate: {}", candidate);
            }
            return key;
        } catch (InterruptedException e) {
            log.error("InterruptedException e: ", e);
            Thread.currentThread().interrupt();
            throw new KafkaProduceException(e);
        } catch (ExecutionException e) {
            log.error("Error when send e: ", e);
            throw new KafkaProduceException(e);
        }
    }

    @Override
    public void save(WbListCandidate candidate) {
        wbListCandidateDao.save(candidate);
    }

    @Override
    public FilterResponse<WbListCandidate> getList(FilterRequest filter) {
        List<WbListCandidate> candidateList = wbListCandidateDao.getList(filter);
        FilterResponse<WbListCandidate> response = new FilterResponse<>();
        response.setResult(candidateList);
        Long lastId = buildLastId(filter.getSize(), candidateList);
        response.setNumericLastId(lastId);
        return response;
    }

    private Long buildLastId(Integer filterSize, List<WbListCandidate> candidates) {
        if (candidates.size() == filterSize) {
            var lastCandidate = candidates.get(candidates.size() - 1);
            return lastCandidate.getId();
        }
        return null;
    }

    @Override
    public void approve(List<Long> ids, String initiator) {
        List<WbListCandidate> candidates = wbListCandidateDao.getByIds(ids);
        for (WbListCandidate candidate : candidates) {
            Row row = rowConverter.convert(candidate);
            ListType listType = ListType.valueOf(candidate.getListType().getLiteral());
            wbListCommandService.sendCommandSync(row, listType, Command.CREATE, initiator);
        }
        log.info("WbListCandidateService send approved candidates with ids: {}", Arrays.toString(ids.toArray()));
        wbListCandidateDao.approve(ids);
    }
}
