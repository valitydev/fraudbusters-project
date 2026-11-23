package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.fraudbusters.api.service.FraudbustersDataService;
import dev.vality.swag.fraudbusters.api.PayoutsApi;
import dev.vality.swag.fraudbusters.model.PayoutsRequest;
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
public class PayoutResource implements PayoutsApi {

    private final FraudbustersDataService fraudbustersService;
    private final Converter<PayoutsRequest, List<Payout>> payoutsRequestToPayoutsConverter;


    @Override
    public ResponseEntity<Void> insertPayouts(PayoutsRequest payoutsRequest) {
        log.debug("-> insertPayouts request: {}", payoutsRequest);
        if (!CollectionUtils.isEmpty(payoutsRequest.getPayouts())) {
            List<Payout> payouts = payoutsRequestToPayoutsConverter.convert(payoutsRequest);
            fraudbustersService.insertPayouts(payouts);
        }
        log.debug("<- insertPayouts success request: {}", payoutsRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
