package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.proxy_inspector.Context;
import dev.vality.fraudbusters.api.service.FraudbustersInspectorService;
import dev.vality.swag.fraudbusters.api.InspectPaymentApi;
import dev.vality.swag.fraudbusters.model.PaymentInspectRequest;
import dev.vality.swag.fraudbusters.model.RiskScore;
import dev.vality.swag.fraudbusters.model.RiskScoreResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class InspectorResource implements InspectPaymentApi {

    private final FraudbustersInspectorService fraudbustersInspectorService;
    private final Converter<PaymentInspectRequest, Context> paymentInspectRequestToContextConverter;

    @Override
    public ResponseEntity<RiskScoreResult> inspectPayment(PaymentInspectRequest paymentInspectRequest) {
        log.debug("-> inspectPayment request: {}", paymentInspectRequest);
        Context context = paymentInspectRequestToContextConverter.convert(paymentInspectRequest);
        RiskScore riskScore = fraudbustersInspectorService.inspectPayment(context);
        RiskScoreResult riskScoreResult = new RiskScoreResult();
        riskScoreResult.setResult(riskScore);
        log.debug("<- inspectPayment riskScoreResult: {}", riskScoreResult);
        return ResponseEntity.ok(riskScoreResult);
    }
}
