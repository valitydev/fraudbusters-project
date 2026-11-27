package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraud_data_crawler.ListType;
import dev.vality.fraudbusters.management.constant.CandidateListName;
import dev.vality.swag.fraudbusters.management.model.Chargeback;
import dev.vality.swag.fraudbusters.management.model.MerchantInfo;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class ChargebackToFraudCandidateConverter {

    private static final String CHARGEBACK_SOURCE = "Chargebacks";

    public FraudDataCandidate toCandidate(Chargeback chargeback, String batchId) {
        if (chargeback.getCardToken() == null || chargeback.getCardToken().isBlank()) {
            return null;
        }
        FraudDataCandidate candidate = prepareCandidate(batchId, chargeback.getMerchantInfo());
        candidate.setValue(chargeback.getCardToken());
        candidate.setType(CandidateListName.CARD_TOKEN);
        return candidate;
    }

    protected FraudDataCandidate prepareCandidate(String batchId, MerchantInfo merchantInfo) {
        FraudDataCandidate candidate = new FraudDataCandidate();
        candidate.setSource(CHARGEBACK_SOURCE);
        candidate.setBatchId(batchId);
        candidate.setListType(ListType.black);
        if (Objects.nonNull(merchantInfo)) {
            candidate.setMerchantId(merchantInfo.getPartyId());
            candidate.setShopId(merchantInfo.getShopId());
        }
        return candidate;
    }
}
