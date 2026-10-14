package dev.vality.fraudbusters.management.converter;

import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.domain.ListRecord;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ListRecordToRowConverter {

    Row destinationToSource(ListRecord destination);
}
