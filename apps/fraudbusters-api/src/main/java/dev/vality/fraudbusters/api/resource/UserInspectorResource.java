package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.InspectUserContext;
import dev.vality.fraudbusters.api.service.FraudbustersInspectorService;
import dev.vality.swag.fraudbusters.api.InspectUserApi;
import dev.vality.swag.fraudbusters.model.UserInspectRequest;
import dev.vality.swag.fraudbusters.model.UserInspectResult;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
public class UserInspectorResource implements InspectUserApi {

    private final FraudbustersInspectorService fraudbustersInspectorService;
    private final Converter<UserInspectRequest, InspectUserContext> userInspectRequestToInspectUserContextConverter;

    @Override
    public ResponseEntity<UserInspectResult> inspectUser(@Valid UserInspectRequest userInspectRequest) {
        log.debug("-> inspectUser request: {}", userInspectRequest);
        InspectUserContext context = userInspectRequestToInspectUserContextConverter.convert(userInspectRequest);
        UserInspectResult result = fraudbustersInspectorService.inspectUser(context);
        log.debug("<- inspectUser result: {}", result);
        return ResponseEntity.ok(result);
    }
}
