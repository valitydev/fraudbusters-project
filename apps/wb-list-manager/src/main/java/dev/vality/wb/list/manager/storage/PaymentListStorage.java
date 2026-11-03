package dev.vality.wb.list.manager.storage;

import dev.vality.damsel.wb_list.Row;
import dev.vality.wb.list.manager.repository.ListRepository;
import dev.vality.wb.list.manager.utils.KeyGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/** Payment rows, including legacy rows that carry party and shop without an identifier. */
@Getter
@RequiredArgsConstructor
public class PaymentListStorage implements ListStorage {

    private final ListRepository repository;

    @Override
    public String key(Row row) {
        return KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue(), partyId(row),
                shopId(row));
    }

    @Override
    public List<String> cascadeKeys(Row row) {
        return List.of(
                KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue()),
                KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue(), partyId(row)),
                key(row));
    }

    private String partyId(Row row) {
        return row.isSetId() ? row.getId().getPaymentId().getPartyId() : row.getPartyId();
    }

    private String shopId(Row row) {
        return row.isSetId() ? row.getId().getPaymentId().getShopId() : row.getShopId();
    }

}
