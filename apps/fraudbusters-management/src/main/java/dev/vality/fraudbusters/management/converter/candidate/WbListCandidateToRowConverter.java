package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class WbListCandidateToRowConverter implements Converter<WbListCandidate, Row> {

    @Override
    public Row convert(WbListCandidate source) {
        return new Row().setId(IdInfo.payment_id(new PaymentId()
                        .setPartyId(source.getPartyId())
                        .setShopId(source.getShopId())))
                .setListName(source.getListName())
                .setValue(source.getValue());
    }
}
