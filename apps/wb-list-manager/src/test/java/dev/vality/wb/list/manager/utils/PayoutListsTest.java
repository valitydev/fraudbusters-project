package dev.vality.wb.list.manager.utils;

import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.P2pId;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.wb.list.manager.handler.WbListServiceHandler;
import dev.vality.wb.list.manager.repository.ListRepository;
import dev.vality.wb.list.manager.storage.ListStorageRegistry;
import dev.vality.wb.list.manager.storage.PaymentListStorage;
import dev.vality.wb.list.manager.storage.PayoutListStorage;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class PayoutListsTest {

    private final ListRepository paymentRepository = mock(ListRepository.class);
    private final ListRepository payoutRepository = mock(ListRepository.class);
    private final PaymentListStorage paymentStorage = new PaymentListStorage(paymentRepository);
    private final PayoutListStorage payoutStorage = new PayoutListStorage(payoutRepository);
    private final WbListServiceHandler handler = new WbListServiceHandler(
            new ListStorageRegistry(List.of(paymentStorage, payoutStorage)), JsonMapper.builder().build());

    @Test
    void legacyPaymentKeysAreUnchangedAndPayoutScopesAreUnambiguous() {
        Row row = createRow(IdInfo.payment_id(new PaymentId()
                .setPartyId("party")
                .setShopId("shop")));
        assertEquals("party_shop_black_USER_ID_synthetic-user", paymentStorage.generateKey(row));
        Row payoutRow = createRow(IdInfo.payout_id(new PayoutId()
                .setPartyId("party")
                .setWalletId("wallet")));
        assertNotEquals(payoutStorage.generateKey(payoutRow),
                KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user", "party_wallet", null));
        assertNotEquals(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user", "a_b", "c"),
                KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user", "a", "b_c"));
    }

    @Test
    void paymentRowWithEmptyIdKeepsLegacyPartyAndShop() {
        Row row = createRow()
                .setPartyId("party")
                .setShopId("shop")
                .setId(new IdInfo());
        assertEquals("party_shop_black_USER_ID_synthetic-user", paymentStorage.generateKey(row));
    }

    @Test
    void storageIsSelectedByRowIdentifierAndP2pIsRejected() {
        ListStorageRegistry registry = new ListStorageRegistry(List.of(paymentStorage, payoutStorage));
        assertSame(paymentStorage, registry.findStorageByRow(createRow()));
        assertSame(paymentStorage, registry.findStorageByRow(createRow(new IdInfo())));
        assertSame(paymentStorage, registry.findStorageByRow(createRow(IdInfo.payment_id(new PaymentId()))));
        assertSame(payoutStorage, registry.findStorageByRow(createRow(IdInfo.payout_id(new PayoutId()))));
        Row p2pRow = createRow(IdInfo.p2p_id(new P2pId()));
        assertThrows(IllegalStateException.class, () -> registry.findStorageByRow(p2pRow));
    }

    @Test
    void payoutFallbackNeverQueriesPaymentStorage() throws Exception {
        Row row = createRow(IdInfo.payout_id(new PayoutId()
                .setPartyId("party")
                .setWalletId("wallet")));
        when(payoutRepository.get(anyString())).thenReturn(Optional.empty());
        assertFalse(handler.isExist(row));
        verifyNoInteractions(paymentRepository);
        verify(payoutRepository).get(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user",
                null, null));
        verify(payoutRepository).get(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user",
                "party", null));
        verify(payoutRepository).get(KeyGenerator.generatePayoutKey(ListType.black, "USER_ID", "synthetic-user",
                "party", "wallet"));
    }

    @Test
    void globalPayoutListMatchesAndWalletWithoutPartyIsRejected() throws Exception {
        Row row = createRow(IdInfo.payout_id(new PayoutId()));
        String global = payoutStorage.generateKey(row);
        when(payoutRepository.get(global)).thenReturn(Optional.of(
                new dev.vality.wb.list.manager.model.Row(global, "synthetic-user")));
        assertTrue(handler.isExist(row));
        verify(payoutRepository, times(1)).get(anyString());
        row.setId(IdInfo.payout_id(new PayoutId().setWalletId("wallet")));
        assertThrows(IllegalArgumentException.class, () -> handler.isExist(row));
        verify(payoutRepository, times(1)).get(anyString());
    }

    private Row createRow() {
        return new Row()
                .setListType(ListType.black)
                .setListName("USER_ID")
                .setValue("synthetic-user");
    }

    private Row createRow(IdInfo id) {
        return createRow()
                .setId(id);
    }
}
