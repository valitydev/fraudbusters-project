package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fistful.destination.DestinationState;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fistful.withdrawal.WithdrawalState;
import dev.vality.fraudbusters.mg.connector.constant.WithdrawalEventType;
import dev.vality.fraudbusters.mg.connector.converter.DominantAccountToDomainAccountConverter;
import dev.vality.fraudbusters.mg.connector.converter.FistfulCashToDomainCashConverter;
import dev.vality.fraudbusters.mg.connector.converter.FistfulResourceToDomainResourceConverter;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.fraudbusters.mg.connector.service.DestinationClientService;
import dev.vality.fraudbusters.mg.connector.service.DominantClientService;
import dev.vality.fraudbusters.mg.connector.service.WithdrawalClientService;
import dev.vality.fraudbusters.mg.connector.utils.WithdrawalModelUtil;
import dev.vality.geck.common.util.TBaseUtil;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalMapper implements Mapper<TimestampedChange, MachineEvent, Withdrawal> {

    private final WithdrawalClientService withdrawalClientService;
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
    public Withdrawal map(TimestampedChange change, MachineEvent event) {
        Withdrawal withdrawal = new Withdrawal();
        final WithdrawalState withdrawalInfo = withdrawalClientService.getWithdrawalInfoFromFistful(
                event.getSourceId(), event.getEventId());
        withdrawalInfo.getDestinationId();
        withdrawal.setCost(fistfulCashToDomainCashConverter.convert(withdrawalInfo.getBody()));
        withdrawal.setEventTime(event.getCreatedAt());
        withdrawal.setId(event.getSourceId());
        withdrawal.setStatus(TBaseUtil.unionFieldToEnum(
                change.getChange().getStatusChanged().getStatus(),
                WithdrawalStatus.class));

        final DestinationState destinationInfo = destinationClientService.getDestinationInfoFromFistful(
                withdrawalInfo.getDestinationId());
        final var walletConfig = dominantClientService.getWalletConfig(
                withdrawalInfo.getWalletId());

        withdrawal.setAccount(accountConverter.convert(walletConfig.getAccount()));

        final Resource resource = fistfulResourceToDomainResourceConverter.convert(destinationInfo.getResource());
        withdrawal.setDestinationResource(resource);
        withdrawal.setProviderInfo(WithdrawalModelUtil.initProviderInfo(withdrawalInfo, destinationInfo));
        withdrawal.setError(WithdrawalModelUtil.initError(change.getChange().getStatusChanged()));
        return withdrawal;
    }

    @Override
    public WithdrawalEventType getChangeType() {
        return WithdrawalEventType.WITHDRAWAL_PAYMENT_CHARGEBACK_STATUS_CHANGED;
    }


}
