package dev.vality.fraudbusters.mg.connector.converter;

import dev.vality.damsel.payment_processing.EventPayload;
import dev.vality.fraudbusters.mg.connector.utils.DeserializerUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentConverterImpl implements BinaryConverter<EventPayload> {

    private ThreadLocal<TDeserializer> threadLocalDeserializer =
            ThreadLocal.withInitial(DeserializerUtils::createDeserializer);

    @Override
    public EventPayload convert(byte[] bin, Class<EventPayload> clazz) {
        EventPayload event = new EventPayload();
        try {
            threadLocalDeserializer.get().deserialize(event, bin);
        } catch (TException e) {
            log.error("BinaryConverterImpl e: ", e);
        }
        return event;
    }

}
