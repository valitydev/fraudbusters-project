package dev.vality.fraudbusters.mg.connector.parser;

import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.fraudbusters.mg.connector.converter.BinaryConverter;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalEventParser implements EventParser<TimestampedChange> {

    private final BinaryConverter<TimestampedChange> converter;

    @Override
    public TimestampedChange parseEvent(MachineEvent message) {
        try {
            byte[] bin = message.getData().getBin();
            return converter.convert(bin, TimestampedChange.class);
        } catch (Exception e) {
            log.error("Exception when parse message e: ", e);
        }
        return null;
    }
}
