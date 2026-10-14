package dev.vality.fraudbusters.api.resource;

import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.damsel.proxy_inspector.InspectorProxySrv;
import dev.vality.fraudbusters.api.config.AbstractKeycloakOpenIdAsWiremockConfig;
import dev.vality.fraudbusters.api.service.FraudbustersDataService;
import dev.vality.fraudbusters.api.service.FraudbustersInspectorService;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import dev.vality.swag.fraudbusters.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ResourceTest extends AbstractKeycloakOpenIdAsWiremockConfig {

    public static final String CHARGEBACKS = "/chargebacks";
    public static final String REFUNDS = "/refunds";
    public static final String PAYMENTS = "/payments";
    public static final String FRAUD_PAYMENTS = "/fraud-payments";
    public static final String WITHDRAWALS = "/withdrawals";
    public static final String INSPECTOR = "/inspect-payment";
    public static final String INSPECT_USER = "/inspect-user";
    public static final String BASE_URL = "http://localhost:";

    @LocalServerPort
    int serverPort;

    @MockitoBean
    FraudbustersDataService fraudbustersDataService;
    @MockitoBean
    FraudbustersInspectorService fraudbustersInspectorService;
    @MockitoBean
    PaymentServiceSrv.Iface paymentServiceSrv;
    @MockitoBean
    InspectorProxySrv.Iface proxyInspectorSrv;

    RestTemplate restTemplate = new RestTemplate();

    @BeforeEach
    void setUp() {
        restTemplate.getInterceptors().add((request, body, execution) -> {
            request.getHeaders().setBearerAuth(generateSimpleJwt());
            return execution.execute(request, body);
        });
    }

    @Test
    void insertChargebacksTest() {
        ChargebacksRequest request = new ChargebacksRequest();
        restTemplate.postForEntity(initUrl(CHARGEBACKS), request, Void.class);
        verify(fraudbustersDataService, times(0)).insertChargebacks(any());

        Chargeback chargeback = new Chargeback();
        request.setChargebacks(List.of(chargeback));
        assertThrows(HttpClientErrorException.BadRequest.class,
                () -> restTemplate.postForEntity(initUrl(CHARGEBACKS), request, Void.class)
        );

        chargeback = ApiBeanGenerator.initChargeback();
        ChargebacksRequest requestFull = new ChargebacksRequest()
                .chargebacks(List.of(chargeback));
        restTemplate.postForEntity(initUrl(CHARGEBACKS), requestFull, Void.class);
        verify(fraudbustersDataService, times(1)).insertChargebacks(any());
    }

    @Test
    void insertRefundsTest() {
        RefundsRequest request = new RefundsRequest();
        restTemplate.postForEntity(initUrl(REFUNDS), request, Void.class);
        verify(fraudbustersDataService, times(0)).insertRefunds(any());

        request.refunds(List.of(new Refund()));
        assertThrows(HttpClientErrorException.BadRequest.class,
                () -> restTemplate.postForEntity(initUrl(REFUNDS), request, Void.class)
        );

        request.refunds(List.of(ApiBeanGenerator.initRefund()));
        restTemplate.postForEntity(initUrl(REFUNDS), request, Void.class);
        verify(fraudbustersDataService, times(1)).insertRefunds(any());
    }

    @Test
    void insertPaymentsTest() {
        PaymentsChangesRequest request = new PaymentsChangesRequest();
        restTemplate.postForEntity(initUrl(PAYMENTS), request, Void.class);
        verify(fraudbustersDataService, times(0)).insertPaymentsChanges(any());

        request.paymentsChanges(List.of(new PaymentChange()));
        assertThrows(HttpClientErrorException.BadRequest.class,
                () -> restTemplate.postForEntity(initUrl(PAYMENTS), request, Void.class)
        );

        request.paymentsChanges(List.of(ApiBeanGenerator.initPaymentChange()));
        restTemplate.postForEntity(initUrl(PAYMENTS), request, Void.class);
        verify(fraudbustersDataService, times(1)).insertPaymentsChanges(any());
    }

    @Test
    void insertFraudPaymentsTest() {
        FraudPaymentsRequest request = new FraudPaymentsRequest();
        restTemplate.postForEntity(initUrl(FRAUD_PAYMENTS), request, Void.class);
        verify(fraudbustersDataService, times(0)).insertFraudPayments(any());

        request.fraudPayments(List.of(new FraudPayment()));
        assertThrows(HttpClientErrorException.BadRequest.class,
                () -> restTemplate.postForEntity(initUrl(FRAUD_PAYMENTS), request, Void.class)
        );

        request.fraudPayments(List.of(ApiBeanGenerator.initFraudPayment()));
        restTemplate.postForEntity(initUrl(FRAUD_PAYMENTS), request, Void.class);
        verify(fraudbustersDataService, times(1)).insertFraudPayments(any());
    }

    @Test
    void insertWithdrawalsTest() {
        WithdrawalsRequest request = new WithdrawalsRequest();
        restTemplate.postForEntity(initUrl(WITHDRAWALS), request, Void.class);
        verify(fraudbustersDataService, times(0)).insertWithdrawals(any());

        request.withdrawals(List.of(new Withdrawal()));
        assertThrows(HttpClientErrorException.BadRequest.class,
                () -> restTemplate.postForEntity(initUrl(WITHDRAWALS), request, Void.class)
        );

        request.withdrawals(List.of(ApiBeanGenerator.initWithdrawal()));
        restTemplate.postForEntity(initUrl(WITHDRAWALS), request, Void.class);
        verify(fraudbustersDataService, times(1)).insertWithdrawals(any());
    }

    @Test
    void inspectPaymentsTest() {
        when(fraudbustersInspectorService.inspectPayment(any())).thenReturn(RiskScore.FATAL);

        PaymentInspectRequest request = new PaymentInspectRequest();
        assertThrows(HttpClientErrorException.BadRequest.class, () ->
                restTemplate.postForEntity(initUrl(INSPECTOR), request, RiskScoreResult.class));

        request.payment(new Payment());
        assertThrows(HttpClientErrorException.BadRequest.class,
                () -> restTemplate.postForEntity(initUrl(INSPECTOR), request, RiskScoreResult.class)
        );

        request.payment(ApiBeanGenerator.initPayment());
        restTemplate.postForEntity(initUrl(INSPECTOR), request, RiskScoreResult.class);
        verify(fraudbustersInspectorService, times(1)).inspectPayment(any());
    }

    @Test
    void inspectUserTest() {
        when(fraudbustersInspectorService.inspectUser(any())).thenReturn(new UserInspectResult());

        UserInspectRequest request = new UserInspectRequest();
        request.setMerchants(null);
        assertThrows(HttpClientErrorException.BadRequest.class, () ->
                restTemplate.postForEntity(initUrl(INSPECT_USER), request, UserInspectResult.class));

        request.customer(ApiBeanGenerator.initCustomer());
        request.merchants(List.of(ApiBeanGenerator.initMerchant()));
        restTemplate.postForEntity(initUrl(INSPECT_USER), request, UserInspectResult.class);
        verify(fraudbustersInspectorService, times(1)).inspectUser(any());
    }

    private String initUrl(String fraudbustersRefunds) {
        return BASE_URL + serverPort + fraudbustersRefunds;
    }

    @TestConfiguration
    static class JwtTestConfig {

        @Bean
        JwtDecoder jwtDecoder() {
            return token -> Jwt.withTokenValue(token)
                    .header("alg", "none")
                    .claim("sub", "test")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        }
    }

}
