package dev.vality.fraudbusters.management.converter.payment;

import dev.vality.fraudbusters.management.domain.ListRecord;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface WbListRecordsToListRecordConverter {

    ListRecord destinationToSource(WbListRecords destination);
}
