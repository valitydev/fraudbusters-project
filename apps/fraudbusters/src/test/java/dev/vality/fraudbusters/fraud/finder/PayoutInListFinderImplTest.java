package dev.vality.fraudbusters.fraud.finder;

import dev.vality.damsel.wb_list.CountInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.Result;
import dev.vality.damsel.wb_list.Row;
import dev.vality.damsel.wb_list.RowInfo;
import dev.vality.damsel.wb_list.WbListServiceSrv;
import dev.vality.fraudbusters.exception.RuleFunctionException;
import dev.vality.fraudbusters.fraud.model.FieldModel;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudbusters.fraud.payout.finder.PayoutInListFinderImpl;
import dev.vality.fraudbusters.repository.clickhouse.impl.PayoutHistoryRepository;
import dev.vality.fraudbusters.util.BeanUtil;
import dev.vality.fraudo.model.Pair;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Instant;
import java.util.List;

import static dev.vality.fraudbusters.fraud.constant.PayoutCheckedField.CARD_TOKEN;
import static dev.vality.fraudbusters.fraud.constant.PayoutCheckedField.EMAIL;
import static dev.vality.fraudbusters.fraud.constant.PayoutCheckedField.USER_ID;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PayoutInListFinderImplTest {

    @Test
    void namedListsUseTheirNameAndPayoutIdentity() throws Exception {
        var lists = mock(WbListServiceSrv.Iface.class);
        final var finder = new PayoutInListFinderImpl(lists, null, BeanUtil.createPayoutFieldsInPayoutHistory());
        var model = new PayoutModel();
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
        final var finder = new PayoutInListFinderImpl(lists, history, BeanUtil.createPayoutFieldsInPayoutHistory());
        var model = new PayoutModel();
        model.setTimestamp(java.time.Instant.parse("2026-01-01T12:00:00Z").toEpochMilli());
        var info = new dev.vality.damsel.wb_list.CountInfo()
                .setCount(1)
                .setTimeToLive("2026-01-02T12:00:00Z");
        when(lists.getRowInfo(any())).thenReturn(new dev.vality.damsel.wb_list.Result()
                .setRowInfo(dev.vality.damsel.wb_list.RowInfo.count_info(info)));
        assertTrue(finder.findInGreyList(List.of(new Pair<>(CARD_TOKEN, "synthetic-card")), model));
        verify(history).countOperationByFieldWithGroupBy(eq("cardToken"), eq("synthetic-card"),
                eq(java.time.Instant.EPOCH.toEpochMilli()), eq(model.getTimestamp()), eq(List.of()));
    }

    private static final String NOW = "2026-01-01T12:00:00Z";

    private final WbListServiceSrv.Iface lists = mock(WbListServiceSrv.Iface.class);
    private final PayoutHistoryRepository history = mock(PayoutHistoryRepository.class);
    private final PayoutInListFinderImpl greyFinder =
            new PayoutInListFinderImpl(lists, history, BeanUtil.createPayoutFieldsInPayoutHistory());

    @Test
    void greyListAllowsOperationsUpToItsCountWithinPartyAndWallet() throws Exception {
        greyRow("2025-12-31T12:00:00Z", "2026-01-02T12:00:00Z", 3);
        var model = payout();
        var card = List.of(new Pair<>(CARD_TOKEN, "synthetic-card"));
        when(history.countOperationByFieldWithGroupBy(anyString(), any(), anyLong(), anyLong(), anyList()))
                .thenReturn(2);
        assertTrue(greyFinder.findInGreyList(card, model));
        when(history.countOperationByFieldWithGroupBy(anyString(), any(), anyLong(), anyLong(), anyList()))
                .thenReturn(3);
        assertFalse(greyFinder.findInGreyList(card, model));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<FieldModel>> scope = ArgumentCaptor.forClass(List.class);
        verify(history, times(2)).countOperationByFieldWithGroupBy(eq("cardToken"), eq("synthetic-card"),
                eq(Instant.parse("2025-12-31T12:00:00Z").toEpochMilli()), eq(model.getTimestamp()),
                scope.capture());
        assertEquals(List.of(new FieldModel("partyId", "synthetic-party"),
                new FieldModel("walletId", "synthetic-wallet")), scope.getValue());
        var row = ArgumentCaptor.forClass(Row.class);
        verify(lists, times(2)).getRowInfo(row.capture());
        assertEquals(ListType.grey, row.getValue().getListType());
        assertEquals("synthetic-wallet", row.getValue().getId().getPayoutId().getWalletId());
    }

    @Test
    void greyListRowsOutsideTheirPeriodDoNotMatchAndAreNotCounted() throws Exception {
        var card = List.of(new Pair<>(CARD_TOKEN, "synthetic-card"));
        greyRow("2026-01-01T13:00:00Z", "2026-01-02T12:00:00Z", 10);
        assertFalse(greyFinder.findInGreyList(card, payout()));
        greyRow("2025-12-31T12:00:00Z", NOW, 10);
        assertFalse(greyFinder.findInGreyList(card, payout()));
        greyRow("2026-01-01T11:00:00Z", "2026-01-01T11:00:00Z", 10);
        assertFalse(greyFinder.findInGreyList(card, payout()));
        verifyNoInteractions(history);
    }

    @Test
    void valueWithoutGreyRowIsNotAMatch() throws Exception {
        when(lists.getRowInfo(any())).thenReturn(new Result());
        assertFalse(greyFinder.findInGreyList(List.of(new Pair<>(CARD_TOKEN, "synthetic-card")), payout()));
        verifyNoInteractions(history);
    }

    @Test
    void missingValuesAreNeverLookedUp() throws Exception {
        var blank = List.of(new Pair<>(EMAIL, " "), new Pair<>(CARD_TOKEN, (String) null));
        assertFalse(greyFinder.findInBlackList(blank, payout()));
        assertFalse(greyFinder.findInWhiteList(blank, payout()));
        assertFalse(greyFinder.findInList("synthetic-list", blank, payout()));
        assertFalse(greyFinder.findInGreyList(blank, payout()));
        verifyNoInteractions(lists, history);
    }

    @Test
    void blackAndWhiteListsAskForOnePresentValueOfTheirType() throws Exception {
        var fields = List.of(new Pair<>(EMAIL, "synthetic@example.com"), new Pair<>(CARD_TOKEN, "synthetic-card"));
        when(lists.isAnyExist(anyList())).thenReturn(false, true);
        assertFalse(greyFinder.findInBlackList(fields, payout()));
        assertTrue(greyFinder.findInWhiteList(fields, payout()));
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Row>> rows = ArgumentCaptor.forClass(List.class);
        verify(lists, times(2)).isAnyExist(rows.capture());
        assertEquals(List.of(ListType.black, ListType.black),
                rows.getAllValues().get(0).stream().map(Row::getListType).toList());
        assertEquals(List.of(ListType.white, ListType.white),
                rows.getAllValues().get(1).stream().map(Row::getListType).toList());
        assertEquals(List.of("EMAIL", "CARD_TOKEN"),
                rows.getAllValues().get(1).stream().map(Row::getListName).toList());
    }

    @Test
    void listServiceFailuresBecomeRuleErrors() throws Exception {
        var fields = List.of(new Pair<>(CARD_TOKEN, "synthetic-card"));
        when(lists.isAnyExist(anyList())).thenThrow(new TException("unavailable"));
        when(lists.getRowInfo(any())).thenThrow(new TException("unavailable"));
        assertThrows(RuleFunctionException.class, () -> greyFinder.findInBlackList(fields, payout()));
        assertThrows(RuleFunctionException.class, () -> greyFinder.findInGreyList(fields, payout()));
    }

    private void greyRow(String start, String timeToLive, int count) throws TException {
        when(lists.getRowInfo(any())).thenReturn(new Result()
                .setRowInfo(RowInfo.count_info(new CountInfo()
                        .setStartCountTime(start)
                        .setTimeToLive(timeToLive)
                        .setCount(count))));
    }

    private PayoutModel payout() {
        var model = new PayoutModel();
        model.setPartyId("synthetic-party");
        model.setWalletId("synthetic-wallet");
        model.setTimestamp(Instant.parse(NOW).toEpochMilli());
        return model;
    }

}
