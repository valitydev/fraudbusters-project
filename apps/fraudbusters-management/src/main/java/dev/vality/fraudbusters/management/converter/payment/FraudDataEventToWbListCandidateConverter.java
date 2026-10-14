package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class FraudDataEventToWbListCandidateConverter implements Converter<FraudDataCandidate, WbListCandidate> {

    @Override
    public WbListCandidate convert(FraudDataCandidate source) {
        WbListCandidate candidate = new WbListCandidate();
        candidate.setValue(source.getValue());
        candidate.setSource(source.getSource());
        candidate.setListType(ListType.valueOf(source.getListType().name()));
        candidate.setListName(source.getType());
        candidate.setPartyId(source.getMerchantId());
        candidate.setShopId(source.getShopId());
        candidate.setBatchId(source.getBatchId());
        return candidate;
    }
}
