package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.damsel.fraudbusters.PayoutStatus;
import dev.vality.fistful.base.ClientIdentity;
import dev.vality.fistful.base.ClientInfo;
import dev.vality.fistful.base.ContactInfo;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.WithdrawalState;
import dev.vality.fistful.withdrawal.status.Failed;
import dev.vality.fistful.withdrawal.status.Pending;
import dev.vality.fistful.withdrawal.status.Status;
import dev.vality.fraudbusters.mg.connector.converter.DominantAccountToDomainAccountConverter;
import dev.vality.fraudbusters.mg.connector.converter.FistfulCashToDomainCashConverter;
import dev.vality.fraudbusters.mg.connector.converter.FistfulCurrencyToDomainCurrencyConverter;
import dev.vality.fraudbusters.mg.connector.converter.FistfulResourceToDomainResourceConverter;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.fraudbusters.mg.connector.service.DestinationClientService;
import dev.vality.fraudbusters.mg.connector.service.DominantClientService;
import dev.vality.fraudbusters.mg.connector.service.PayoutClientService;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static dev.vality.fraudbusters.mg.connector.mapper.impl.PayoutBeanUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;


@Slf4j
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {DominantAccountToDomainAccountConverter.class,
        FistfulCashToDomainCashConverter.class,
        FistfulCurrencyToDomainCurrencyConverter.class,
        FistfulResourceToDomainResourceConverter.class,
        LogPayoutMapperDecorator.class,
        PayoutMapper.class})
public class PayoutMapperTest {

    public static final String SOURCE_ID = "SOURCE_ID";
    public static final Long EVENT_ID = 1L;
    public static final String WALLET_ID = "walletId";
    public static final String DESTINATION_ID = "destinationId";

    @MockitoBean
    DestinationClientService destinationClientService;

    @MockitoBean
    PayoutClientService payoutClientService;

    @MockitoBean
    DominantClientService dominantClientService;

    @Autowired
    Mapper<TimestampedChange, MachineEvent, Payout> logPayoutMapperDecorator;

    @Test
    void accept() {
        final Status failed = Status.failed(new Failed());
        TimestampedChange timestampedChange = createStatusChange(failed);
        boolean accept = logPayoutMapperDecorator.accept(timestampedChange);
        assertTrue(accept);

        timestampedChange = createStatusChange(Status.pending(new Pending()));
        accept = logPayoutMapperDecorator.accept(timestampedChange);
        assertFalse(accept);
    }

    @Test
    void map() {
        final MachineEvent event = new MachineEvent();
        event.setSourceId(SOURCE_ID);
        event.setEventId(EVENT_ID);

        final WithdrawalState payoutState = new WithdrawalState();
        payoutState.setBody(createCash());
        payoutState.setDestinationId(DESTINATION_ID);
        payoutState.setWalletId(WALLET_ID);
        when(payoutClientService.getPayoutInfoFromFistful(SOURCE_ID, EVENT_ID)).thenReturn(payoutState);
        when(dominantClientService.getWalletConfig(WALLET_ID)).thenReturn(createWalletConfigObject().getData());
        when(destinationClientService.getDestinationInfoFromFistful(DESTINATION_ID))
                .thenReturn(createDestinationState());

        TimestampedChange timestampedChange = createStatusChange(Status.failed(new Failed()));
        final Payout map = logPayoutMapperDecorator.map(timestampedChange, event);

        assertEquals(RUB, map.getCost().getCurrency().symbolic_code);
        assertEquals(PayoutStatus.failed, map.getStatus());
        assertEquals(SOURCE_ID, map.getId());
        assertTrue(map.getDestinationResource().isSetBankCard());
        assertEquals(WALLET_ACCOUNT_ID, Long.valueOf(map.getAccount().getId()));
        assertEquals(RUB, map.getAccount().getCurrency().symbolic_code);
        assertFalse(map.isSetClientInfo());
    }

    @Test
    void mapClientFields() {
        final MachineEvent event = new MachineEvent();
        event.setSourceId(SOURCE_ID);
        event.setEventId(EVENT_ID);

        final WithdrawalState payoutState = new WithdrawalState();
        payoutState.setBody(createCash());
        payoutState.setDestinationId(DESTINATION_ID);
        payoutState.setWalletId(WALLET_ID);
        payoutState.setContactInfo(new ContactInfo()
                .setEmail("synthetic@example.com")
                .setPhoneNumber("synthetic-phone"));
        payoutState.setClientInfo(new ClientInfo()
                .setIpAddress("192.0.2.1")
                .setFingerprint("synthetic-fingerprint"));
        payoutState.setClientIdentity(new ClientIdentity()
                .setCustomerId("synthetic-customer")
                .setUserId("synthetic-user"));
        when(payoutClientService.getPayoutInfoFromFistful(SOURCE_ID, EVENT_ID)).thenReturn(payoutState);
        when(dominantClientService.getWalletConfig(WALLET_ID)).thenReturn(createWalletConfigObject().getData());
        when(destinationClientService.getDestinationInfoFromFistful(DESTINATION_ID))
                .thenReturn(createDestinationState());

        final var clientInfo = logPayoutMapperDecorator
                .map(createStatusChange(Status.failed(new Failed())), event)
                .getClientInfo();

        assertEquals("synthetic@example.com", clientInfo.getEmail());
        assertEquals("synthetic-phone", clientInfo.getPhone());
        assertEquals("192.0.2.1", clientInfo.getIp());
        assertEquals("synthetic-fingerprint", clientInfo.getFingerprint());
        assertEquals("synthetic-customer", clientInfo.getCustomerId());
        assertEquals("synthetic-user", clientInfo.getUserId());
    }
}
