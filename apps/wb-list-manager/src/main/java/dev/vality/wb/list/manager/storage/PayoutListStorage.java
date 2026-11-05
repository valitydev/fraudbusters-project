package dev.vality.wb.list.manager.storage;

import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.wb.list.manager.repository.ListRepository;
import dev.vality.wb.list.manager.utils.KeyGenerator;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class PayoutListStorage implements ListStorage {

    private final ListRepository repository;

    @Override
    public String generateKey(Row row) {
        PayoutId id = row.getId().getPayoutId();
        return KeyGenerator.generatePayoutKey(row.getListType(), row.getListName(), row.getValue(),
                id.getPartyId(), id.getWalletId());
    }

    @Override
    public List<String> generateCascadeKeys(Row row) {
        // The exact key is built first so that an invalid scope fails before the global fallback is read.
        String walletKey = generateKey(row);
        String partyId = row.getId().getPayoutId().getPartyId();
        return List.of(
                KeyGenerator.generatePayoutKey(row.getListType(), row.getListName(), row.getValue(), null, null),
                KeyGenerator.generatePayoutKey(row.getListType(), row.getListName(), row.getValue(), partyId, null),
                walletKey);
    }

}
