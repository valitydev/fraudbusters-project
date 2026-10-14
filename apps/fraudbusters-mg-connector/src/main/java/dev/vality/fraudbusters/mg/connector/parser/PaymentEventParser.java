package dev.vality.fraudbusters.mg.connector.parser;

import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.fraudbusters.mg.connector.converter.BinaryConverter;
import dev.vality.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentEventParser implements EventParser<EventPayload> {

    private final BinaryConverter<EventPayload> converter;

    @Override
    public EventPayload parseEvent(MachineEvent message) {
        try {
            byte[] bin = message.getData().getBin();
            return converter.convert(bin, EventPayload.class);
        } catch (Exception e) {
            log.error("Exception when parse message e: ", e);
        }
        return null;
    }
}
