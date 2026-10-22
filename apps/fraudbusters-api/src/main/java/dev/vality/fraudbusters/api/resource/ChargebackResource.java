package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.Chargeback;
import dev.vality.fraudbusters.api.service.FraudbustersDataService;
import dev.vality.swag.fraudbusters.api.ChargebacksApi;
import dev.vality.swag.fraudbusters.model.ChargebacksRequest;
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
public class ChargebackResource implements ChargebacksApi {

    private final FraudbustersDataService fraudbustersService;
    private final Converter<ChargebacksRequest, List<Chargeback>> chargebacksRequestToChargebacksConverter;

    @Override
    public ResponseEntity<Void> insertChargebacks(ChargebacksRequest chargebacksRequest) {
        log.debug("-> insertChargebacks request: {}", chargebacksRequest);
        if (!CollectionUtils.isEmpty(chargebacksRequest.getChargebacks())) {
            List<Chargeback> chargebacks = chargebacksRequestToChargebacksConverter.convert(chargebacksRequest);
            fraudbustersService.insertChargebacks(chargebacks);
        }
        log.debug("<- insertChargebacks success request: {}", chargebacksRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
