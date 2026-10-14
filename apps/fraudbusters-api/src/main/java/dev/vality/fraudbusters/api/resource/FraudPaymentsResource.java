package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.FraudPayment;
import dev.vality.fraudbusters.api.service.FraudbustersDataService;
import dev.vality.swag.fraudbusters.api.FraudPaymentsApi;
import dev.vality.swag.fraudbusters.model.FraudPaymentsRequest;
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
public class FraudPaymentsResource implements FraudPaymentsApi {

    private final FraudbustersDataService fraudbustersService;
    private final Converter<FraudPaymentsRequest, List<FraudPayment>> fraudPaymentsRequestToFraudPaymentsConverter;

    @Override
    public ResponseEntity<Void> insertFraudPayments(FraudPaymentsRequest fraudPaymentsRequest) {
        log.debug("-> insertFraudPayments request: {}", fraudPaymentsRequest);
        if (!CollectionUtils.isEmpty(fraudPaymentsRequest.getFraudPayments())) {
            List<FraudPayment> fraudPayments =
                    fraudPaymentsRequestToFraudPaymentsConverter.convert(fraudPaymentsRequest);
            fraudbustersService.insertFraudPayments(fraudPayments);
        }
        log.debug("<- insertFraudPayments success request: {}", fraudPaymentsRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
