package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.Refund;
import dev.vality.fraudbusters.api.service.FraudbustersDataService;
import dev.vality.swag.fraudbusters.api.RefundsApi;
import dev.vality.swag.fraudbusters.model.RefundsRequest;
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
public class RefundResource implements RefundsApi {

    private final FraudbustersDataService fraudbustersService;
    private final Converter<RefundsRequest, List<Refund>> refundsRequestToRefundsConverter;

    @Override
    public ResponseEntity<Void> insertRefunds(RefundsRequest refundsRequest) {
        log.debug("-> insertRefunds request: {}", refundsRequest);
        if (!CollectionUtils.isEmpty(refundsRequest.getRefunds())) {
            List<Refund> refunds = refundsRequestToRefundsConverter.convert(refundsRequest);
            fraudbustersService.insertRefunds(refunds);
        }
        log.debug("<- insertRefunds success request: {}", refundsRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }
}
