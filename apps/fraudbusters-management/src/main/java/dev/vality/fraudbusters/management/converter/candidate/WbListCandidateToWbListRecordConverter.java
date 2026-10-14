package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.fraudbusters.management.domain.tables.pojos.WbListCandidate;
import dev.vality.swag.fraudbusters.management.model.WbListRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class WbListCandidateToWbListRecordConverter {

    public List<WbListRecord> toWbListRecord(List<WbListCandidate> source) {
        return source.stream()
                .map(this::toWbListRecord)
                .collect(Collectors.toList());
    }


    public WbListRecord toWbListRecord(WbListCandidate source) {
        return new WbListRecord()
                .insertTime(source.getInsertTime())
                .listName(source.getListName())
                .listType(WbListRecord.ListTypeEnum.fromValue(source.getListType().name()))
                .partyId(source.getPartyId())
                .shopId(source.getShopId())
                .value(source.getValue())
                .id(String.valueOf(source.getId()));
    }
}
