package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fraudbusters.mg.connector.constant.PayoutEventType;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogPayoutMapperDecorator implements Mapper<TimestampedChange, MachineEvent, Payout> {

    private final PayoutMapper payoutMapper;

    @Override
    public boolean accept(TimestampedChange change) {
        return payoutMapper.accept(change);
    }

    @Override
    public Payout map(TimestampedChange change, MachineEvent event) {
        log.debug("Payout map from change: {} event: {} ", change, event);
        Payout payout = payoutMapper.map(change, event);
        log.debug("Payout map result: {}", payout);
        return payout;
    }

    @Override
    public PayoutEventType getChangeType() {
        return payoutMapper.getChangeType();
    }

}
