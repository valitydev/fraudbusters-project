package dev.vality.fraudbusters.management.dao.payment.wblist;

import dev.vality.fraudbusters.management.dao.CdDao;
import dev.vality.fraudbusters.management.domain.enums.ListType;
import dev.vality.fraudbusters.management.domain.request.FilterRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import org.springframework.lang.NonNull;

import java.time.LocalDateTime;
import java.util.List;

public interface WbListDao extends CdDao<WbListRecords> {

    void saveListRecord(WbListRecords listRecord);

    void removeRecord(WbListRecords listRecord);

    WbListRecords getById(String id);

    List<WbListRecords> getFilteredListRecords(String partyId, String shopId, ListType listType, String listName);

    List<WbListRecords> filterListRecords(ListType listType, List<String> listNames, FilterRequest filterRequest);

    Integer countFilterRecords(@NonNull ListType listType, @NonNull List<String> listNames, String filterValue);

    List<String> getCurrentListNames(ListType listType);

    List<WbListRecords> getRottenRecords(LocalDateTime thresholdRotDate);
}
