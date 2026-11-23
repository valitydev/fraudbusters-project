package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.damsel.fraudbusters.PayoutStatus;
import dev.vality.fistful.destination.DestinationState;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.WithdrawalState;
import dev.vality.fraudbusters.mg.connector.constant.PayoutEventType;
import dev.vality.fraudbusters.mg.connector.converter.DominantAccountToDomainAccountConverter;
import dev.vality.fraudbusters.mg.connector.converter.FistfulCashToDomainCashConverter;
import dev.vality.fraudbusters.mg.connector.converter.FistfulResourceToDomainResourceConverter;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.fraudbusters.mg.connector.service.DestinationClientService;
import dev.vality.fraudbusters.mg.connector.service.DominantClientService;
import dev.vality.fraudbusters.mg.connector.service.PayoutClientService;
import dev.vality.fraudbusters.mg.connector.utils.PayoutModelUtil;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayoutMapper implements Mapper<TimestampedChange, MachineEvent, Payout> {

    private final PayoutClientService payoutClientService;
    private final DestinationClientService destinationClientService;
    private final DominantClientService dominantClientService;
    private final FistfulResourceToDomainResourceConverter fistfulResourceToDomainResourceConverter;
    private final DominantAccountToDomainAccountConverter accountConverter;
    private final FistfulCashToDomainCashConverter fistfulCashToDomainCashConverter;

    @Override
    public boolean accept(TimestampedChange change) {
        return change.getChange().isSetStatusChanged()
                && change.getChange().getStatusChanged().isSetStatus()
                && (change.getChange().getStatusChanged().getStatus().isSetFailed()
                || change.getChange().getStatusChanged().getStatus().isSetSucceeded());
    }

    @Override
    public Payout map(TimestampedChange change, MachineEvent event) {
        Payout payout = new Payout();
        final WithdrawalState payoutInfo = payoutClientService.getPayoutInfoFromFistful(
                event.getSourceId(), event.getEventId());
        payoutInfo.getDestinationId();
        payout.setCost(fistfulCashToDomainCashConverter.convert(payoutInfo.getBody()));
        payout.setEventTime(event.getCreatedAt());
        payout.setId(event.getSourceId());
        payout.setStatus(TBaseUtil.unionFieldToEnum(
                change.getChange().getStatusChanged().getStatus(),
                PayoutStatus.class));

        final DestinationState destinationInfo = destinationClientService.getDestinationInfoFromFistful(
                payoutInfo.getDestinationId());
        final var walletConfig = dominantClientService.getWalletConfig(
                payoutInfo.getWalletId());

        payout.setAccount(accountConverter.convert(walletConfig.getAccount()));
        payout.setWalletId(payoutInfo.getWalletId());
        if (walletConfig.isSetPartyRef()) {
            payout.setPartyId(walletConfig.getPartyRef().getId());
        }

        final Resource resource = fistfulResourceToDomainResourceConverter.convert(destinationInfo.getResource());
        payout.setDestinationResource(resource);
        payout.setProviderInfo(PayoutModelUtil.initProviderInfo(payoutInfo, destinationInfo));
        payout.setError(PayoutModelUtil.initError(change.getChange().getStatusChanged()));
        payout.setClientInfo(PayoutModelUtil.initClientInfo(payoutInfo));
        return payout;
    }

    @Override
    public PayoutEventType getChangeType() {
        return PayoutEventType.PAYOUT_PAYMENT_CHARGEBACK_STATUS_CHANGED;
    }


}
