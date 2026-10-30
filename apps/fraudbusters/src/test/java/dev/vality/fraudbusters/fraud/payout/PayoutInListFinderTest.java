package dev.vality.fraudbusters.fraud.payout;

import dev.vality.damsel.wb_list.Row;
import dev.vality.damsel.wb_list.WbListServiceSrv;
import dev.vality.fraudbusters.fraud.model.PaymentModel;
import dev.vality.fraudo.model.Pair;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static dev.vality.fraudbusters.fraud.constant.PaymentCheckedField.USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class PayoutInListFinderTest {

    @Test
    void namedListsUseTheirNameAndPayoutIdentity() throws Exception {
        var lists = mock(WbListServiceSrv.Iface.class);
        final var finder = new PayoutInListFinder(lists, null);
        var model = new PaymentModel();
        model.setPartyId("synthetic-party");
        model.setWalletId("synthetic-wallet");
        when(lists.isAnyExist(anyList())).thenReturn(true);
        assertTrue(finder.findInList("synthetic-list", List.of(new Pair<>(USER_ID, "synthetic-user")), model));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Row>> rows = ArgumentCaptor.forClass(List.class);
        verify(lists).isAnyExist(rows.capture());
        var row = rows.getValue().get(0);
        assertEquals("synthetic-list", row.getListName());
        assertTrue(row.getId().isSetPayoutId());
        assertEquals("synthetic-wallet", row.getId().getPayoutId().getWalletId());
        assertFalse(row.getId().isSetPaymentId());
    }

    @Test
    void greyListSupportsAnOmittedStartTime() throws Exception {
        var lists = mock(WbListServiceSrv.Iface.class);
        var history = mock(dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository.class);
        final var finder = new PayoutInListFinder(lists, history);
        var model = new PaymentModel();
        model.setTimestamp(java.time.Instant.parse("2026-01-01T12:00:00Z").toEpochMilli());
        var info = new dev.vality.damsel.wb_list.CountInfo().setCount(1)
                .setTimeToLive("2026-01-02T12:00:00Z");
        when(lists.getRowInfo(any())).thenReturn(new dev.vality.damsel.wb_list.Result()
                .setRowInfo(dev.vality.damsel.wb_list.RowInfo.count_info(info)));
        assertTrue(finder.findInGreyList(List.of(new Pair<>(USER_ID, "synthetic-user")), model));
        var bound = ArgumentCaptor.forClass(dev.vality.fraudbusters.domain.TimeBound.class);
        verify(history).aggregate(eq("count()"), anyList(), bound.capture(), isNull(), isNull());
        assertEquals(java.time.Instant.EPOCH, bound.getValue().getLeft());
    }

}
