package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.fraudbusters.api.service.FraudbustersDataService;
import dev.vality.swag.fraudbusters.api.PaymentsApi;
import dev.vality.swag.fraudbusters.model.PaymentsChangesRequest;
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
public class PaymentResource implements PaymentsApi {

    private final FraudbustersDataService fraudbustersService;
    private final Converter<PaymentsChangesRequest, List<Payment>> paymentsChangesRequestToPaymentsConverter;

    @Override
    public ResponseEntity<Void> insertPaymentsChanges(PaymentsChangesRequest paymentsChangesRequest) {
        log.debug("-> insertPaymentsChanges request: {}", paymentsChangesRequest);
        if (!CollectionUtils.isEmpty(paymentsChangesRequest.getPaymentsChanges())) {
            List<Payment> payments = paymentsChangesRequestToPaymentsConverter.convert(paymentsChangesRequest);
            fraudbustersService.insertPaymentsChanges(payments);
        }
        log.debug("<- insertPaymentsChanges success request: {}", paymentsChangesRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
