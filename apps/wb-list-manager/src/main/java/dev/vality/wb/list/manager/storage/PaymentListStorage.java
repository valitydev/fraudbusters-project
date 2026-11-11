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

    /** A row without an identifier keeps its historical meaning: payment. */
    @Override
    public boolean canHandle(Row row) {
        return !row.isSetId() || !row.getId().isSet() || row.getId().isSetPaymentId();
    }

    @Override
    public String generateKey(Row row) {
        return KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue(), extractPartyId(row),
                extractShopId(row));
    }

    @Override
    public List<String> generateCascadeKeys(Row row) {
        return List.of(
                KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue()),
                KeyGenerator.generateKey(row.getListType(), row.getListName(), row.getValue(), extractPartyId(row)),
                generateKey(row));
    }

    private String extractPartyId(Row row) {
        return hasPaymentId(row) ? row.getId().getPaymentId().getPartyId() : row.getPartyId();
    }

    private String extractShopId(Row row) {
        return hasPaymentId(row) ? row.getId().getPaymentId().getShopId() : row.getShopId();
    }

    private static boolean hasPaymentId(Row row) {
        return row.isSetId() && row.getId().isSetPaymentId();
    }

}
