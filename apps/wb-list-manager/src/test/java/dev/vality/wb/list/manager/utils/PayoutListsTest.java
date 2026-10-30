package dev.vality.wb.list.manager.utils;

import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.wb.list.manager.handler.WbListServiceHandler;
import dev.vality.wb.list.manager.repository.ListRepository;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PayoutListsTest {

    @Test
    void legacyPaymentKeysAreUnchangedAndPayoutScopesAreUnambiguous() {
        Row row = row().setId(IdInfo.payment_id(new PaymentId().setPartyId("party").setShopId("shop")));
        assertEquals("party_shop_black_USER_ID_synthetic-user", KeyGenerator.generateKey(row));
        row.setId(IdInfo.payout_id(new PayoutId().setPartyId("party").setWalletId("wallet")));
        assertNotEquals(KeyGenerator.generateKey(row),
                KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user", "party_wallet", null));
        assertNotEquals(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user", "a_b", "c"),
                KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user", "a", "b_c"));
    }

    @Test
    void payoutFallbackNeverQueriesPaymentStorage() throws Exception {
        var repository = mock(ListRepository.class);
        var handler = new WbListServiceHandler(repository, JsonMapper.builder().build());
        Row row = row().setId(IdInfo.payout_id(new PayoutId().setPartyId("party").setWalletId("wallet")));
        when(repository.get(anyString(), eq(true))).thenReturn(Optional.empty());
        assertFalse(handler.isExist(row));
        verify(repository, never()).get(anyString());
        verify(repository).get(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user",
                null, null), true);
        verify(repository).get(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user",
                "party", null), true);
        verify(repository).get(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user",
                "party", "wallet"), true);
    }

    @Test
    void globalPayoutListMatchesAndWalletWithoutPartyIsRejected() throws Exception {
        var repository = mock(ListRepository.class);
        var handler = new WbListServiceHandler(repository, JsonMapper.builder().build());
        Row row = row().setId(IdInfo.payout_id(new PayoutId()));
        String global = KeyGenerator.generateKey(row);
        when(repository.get(global, true)).thenReturn(Optional.of(
                new dev.vality.wb.list.manager.model.Row(global, "synthetic-user", true)));
        assertTrue(handler.isExist(row));
        verify(repository, times(1)).get(global, true);
        row.setId(IdInfo.payout_id(new PayoutId().setWalletId("wallet")));
        assertThrows(IllegalArgumentException.class, () -> handler.isExist(row));
    }

    private Row row() {
        return new Row().setListType(ListType.black).setListName("USER_ID").setValue("synthetic-user");
    }
}
