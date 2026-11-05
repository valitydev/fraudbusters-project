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
    public String generateKey(Row row) {
        return KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue(), getPartyId(row),
                getShopId(row));
    }

    @Override
    public List<String> generateCascadeKeys(Row row) {
        return List.of(
                KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue()),
                KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue(), getPartyId(row)),
                generateKey(row));
    }

    private String getPartyId(Row row) {
        return row.isSetId() ? row.getId().getPaymentId().getPartyId() : row.getPartyId();
    }

    private String getShopId(Row row) {
        return row.isSetId() ? row.getId().getPaymentId().getShopId() : row.getShopId();
    }

}
