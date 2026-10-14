package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.constant.CandidateListName;
import dev.vality.swag.fraudbusters.management.model.Chargeback;
import dev.vality.swag.fraudbusters.management.model.ClientInfo;
import org.springframework.stereotype.Component;

@Component
public class ChargebackToFingerprintFraudCandidateConverter extends ChargebackToFraudCandidateConverter {

    @Override
    public FraudDataCandidate toCandidate(Chargeback chargeback, String batchId) {
        FraudDataCandidate candidate = prepareCandidate(batchId, chargeback.getMerchantInfo());
        ClientInfo clientInfo = chargeback.getClientInfo();
        candidate.setValue(clientInfo.getFingerprint());
        candidate.setType(CandidateListName.FINGERPRRINT);
        return candidate;
    }
}
