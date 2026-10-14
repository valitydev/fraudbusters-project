package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.fraudbusters.management.service.payment.PaymentLoadDataService;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PaymentsLoadDataApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentLoadDataResource implements PaymentsLoadDataApi {

    private final PaymentLoadDataService paymentLoadDataService;
    private final UserInfoService userInfoService;

    @Override
    @PreAuthorize("hasAnyRole('fraud-officer')")
    public ResponseEntity<Void> loadFraudPayments(MultipartFile file) {
        String userName = userInfoService.getUserName();
        log.info("-> loadFraudPayments initiator: {} fileName: {}", userName, file.getName());
        paymentLoadDataService.loadFraudPayments(file, userName);
        log.info("<- loadFraudPayments success load");
        return ResponseEntity.ok().build();
    }

}
