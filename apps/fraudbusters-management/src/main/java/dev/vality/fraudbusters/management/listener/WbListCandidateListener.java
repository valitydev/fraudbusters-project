package dev.vality.fraudbusters.management.listener;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateBatchService;
import dev.vality.fraudbusters.management.service.iface.WbListCandidateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WbListCandidateListener {

    private final Converter<FraudDataCandidate, WbListCandidate> candidateConverter;
    private final WbListCandidateService wbListCandidateService;
    private final WbListCandidateBatchService wbListCandidateBatchService;

    @KafkaListener(
            topics = "${kafka.topic.wblist.candidate}",
            containerFactory = "kafkaFraudCandidateListenerContainerFactory")
    public void listen(FraudDataCandidate candidate) {
        log.info("WbListCandidateListener receive candidate: {}", candidate);
        wbListCandidateBatchService.save(candidate.getBatchId(), candidate.getSource());
        WbListCandidate wbListCandidate = candidateConverter.convert(candidate);
        wbListCandidateService.save(wbListCandidate);
    }
}
