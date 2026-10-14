package dev.vality.fraudbusters.mg.connector.utils;

import dev.vality.kafka.common.exception.TransportException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.thrift.TDeserializer;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TTransportException;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DeserializerUtils {

    public static TDeserializer createDeserializer() {
        try {
            return new TDeserializer(new TBinaryProtocol.Factory());
        } catch (TTransportException ex) {
            throw new TransportException(ex);
        }
    }

}
