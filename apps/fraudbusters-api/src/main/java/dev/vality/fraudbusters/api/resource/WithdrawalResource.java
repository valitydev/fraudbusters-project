package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.fraudbusters.api.service.FraudbustersDataService;
import dev.vality.swag.fraudbusters.api.WithdrawalsApi;
import dev.vality.swag.fraudbusters.model.WithdrawalsRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
public class WithdrawalResource implements WithdrawalsApi {

    private final FraudbustersDataService fraudbustersService;
    private final Converter<WithdrawalsRequest, List<Withdrawal>> withdrawalsRequestToWithdrawalsConverter;


    @Override
    public ResponseEntity<Void> insertWithdrawals(WithdrawalsRequest withdrawalsRequest) {
        log.debug("-> insertWithdrawals request: {}", withdrawalsRequest);
        if (!CollectionUtils.isEmpty(withdrawalsRequest.getWithdrawals())) {
            List<Withdrawal> withdrawals = withdrawalsRequestToWithdrawalsConverter.convert(withdrawalsRequest);
            fraudbustersService.insertWithdrawals(withdrawals);
        }
        log.debug("<- insertWithdrawals success request: {}", withdrawalsRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
