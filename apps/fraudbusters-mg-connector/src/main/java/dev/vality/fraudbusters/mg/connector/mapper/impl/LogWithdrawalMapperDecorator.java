package dev.vality.fraudbusters.mg.connector.mapper.impl;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fraudbusters.mg.connector.constant.WithdrawalEventType;
import dev.vality.fraudbusters.mg.connector.mapper.Mapper;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LogWithdrawalMapperDecorator implements Mapper<TimestampedChange, MachineEvent, Withdrawal> {

    private final WithdrawalMapper withdrawalMapper;

    @Override
    public boolean accept(TimestampedChange change) {
        return withdrawalMapper.accept(change);
    }

    @Override
    public Withdrawal map(TimestampedChange change, MachineEvent event) {
        log.debug("Mapping withdrawal event: {}", event.getEventId());
        Withdrawal withdrawal = withdrawalMapper.map(change, event);
        log.debug("Mapped withdrawal status: {}", withdrawal.getStatus());
        return withdrawal;
    }

    @Override
    public WithdrawalEventType getChangeType() {
        return withdrawalMapper.getChangeType();
    }

}
