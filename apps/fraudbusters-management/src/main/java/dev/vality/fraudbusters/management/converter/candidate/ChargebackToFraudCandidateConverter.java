package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraud_data_crawler.ListType;
import dev.vality.swag.fraudbusters.management.model.Chargeback;
import dev.vality.swag.fraudbusters.management.model.MerchantInfo;

import java.util.Objects;

public abstract class ChargebackToFraudCandidateConverter {

    private static final String CHARGEBACK_SOURCE = "Chargebacks";

    public abstract FraudDataCandidate toCandidate(Chargeback chargeback, String batchId);

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
