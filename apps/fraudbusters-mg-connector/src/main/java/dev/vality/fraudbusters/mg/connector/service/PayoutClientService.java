package dev.vality.fraudbusters.mg.connector.service;


import dev.vality.fistful.withdrawal.ManagementSrv;
import dev.vality.fistful.withdrawal.WithdrawalState;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoNotFoundException;
import dev.vality.fraudbusters.mg.connector.exception.PaymentInfoRequestException;
import dev.vality.fraudbusters.mg.connector.factory.FistfulEventRangeFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutClientService {

    private final ManagementSrv.Iface payoutClient;
    private final FistfulEventRangeFactory fistfulEventRangeFactory;

    public WithdrawalState getPayoutInfoFromFistful(String eventId, long sequenceId) {
        try {
            final WithdrawalState payoutState =
                    payoutClient.get(eventId, fistfulEventRangeFactory.create(sequenceId));
            if (payoutState == null) {
                throw new PaymentInfoNotFoundException("Not found invoice info in hg!");
            }
            return payoutState;
        } catch (TException e) {
            log.error("Error when getPayoutInfoFromFistful eventId: {} sequenceId: {} e: ", eventId, sequenceId, e);
            throw new PaymentInfoRequestException(e);
        }
    }
}
