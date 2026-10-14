package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.swag.fraudbusters.management.model.Chargeback;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChargebacksToFraudDataCandidatesConverter {

    private final List<ChargebackToFraudCandidateConverter> chargebackToFraudCandidateConverters;

    public List<FraudDataCandidate> toCandidates(List<Chargeback> chargebacks, String batchId) {
        return chargebacks.stream()
                .flatMap(chargeback -> toCandidates(chargeback, batchId).stream())
                .collect(Collectors.toList());
    }

    private List<FraudDataCandidate> toCandidates(Chargeback chargeback, String batchId) {
        return chargebackToFraudCandidateConverters.stream()
                .map(converter -> converter.toCandidate(chargeback, batchId))
                .collect(Collectors.toList());
    }

}
