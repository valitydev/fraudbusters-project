package dev.vality.fraudbusters.mg.connector.service;


import dev.vality.fistful.base.EventRange;
import dev.vality.fistful.destination.DestinationState;
import dev.vality.fistful.destination.ManagementSrv;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoNotFoundException;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoRequestException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class DestinationClientService {

    private final ManagementSrv.Iface destinationClient;

    public DestinationState getDestinationInfoFromFistful(String eventId) {
        try {
            final DestinationState destinationState = destinationClient.get(eventId, new EventRange());
            if (destinationState == null) {
                throw new PaymentInfoNotFoundException("Not found invoice info in hg!");
            }
            return destinationState;
        } catch (TException e) {
            log.error("Error when getDestinationInfoFromFistful eventId: {} e: ", eventId, e);
            throw new PaymentInfoRequestException(e);
        }
    }
}
