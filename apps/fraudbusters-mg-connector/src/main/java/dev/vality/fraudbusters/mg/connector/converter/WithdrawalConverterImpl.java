package dev.vality.fraudbusters.mg.connector.converter;

import dev.vality.fistful.withdrawal.TimestampedChange;
import dev.vality.kafka.common.exception.TransportException;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransportException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WithdrawalConverterImpl implements BinaryConverter<TimestampedChange> {

    private ThreadLocal<TDeserializer> threadLocalDeserializer = ThreadLocal.withInitial(this::createDeserializer);

    @Override
    public TimestampedChange convert(byte[] bin, Class<TimestampedChange> clazz) {
        TimestampedChange event = new TimestampedChange();
        try {
            threadLocalDeserializer.get().deserialize(event, bin);
        } catch (TException e) {
            log.error("Error when convert TimestampedChange e: ", e);
        }
        return event;
    }

    private TDeserializer createDeserializer() {
        try {
            return new TDeserializer(new TBinaryProtocol.Factory());
        } catch (TTransportException ex) {
            throw new TransportException(ex);
        }
    }

}
