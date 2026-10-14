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
public class WithdrawalClientService {

    private final ManagementSrv.Iface withdrawalClient;
    private final FistfulEventRangeFactory fistfulEventRangeFactory;

    public WithdrawalState getWithdrawalInfoFromFistful(String eventId, long sequenceId) {
        try {
            final WithdrawalState withdrawalState =
                    withdrawalClient.get(eventId, fistfulEventRangeFactory.create(sequenceId));
            if (withdrawalState == null) {
                throw new PaymentInfoNotFoundException("Not found invoice info in hg!");
            }
            return withdrawalState;
        } catch (TException e) {
            log.error("Error when getWithdrawalInfoFromFistful eventId: {} sequenceId: {} e: ", eventId, sequenceId, e);
            throw new PaymentInfoRequestException(e);
        }
    }
}
