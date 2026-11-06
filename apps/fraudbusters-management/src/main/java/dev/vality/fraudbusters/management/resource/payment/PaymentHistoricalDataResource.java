package dev.vality.fraudbusters.management.resource.payment;

import dev.vality.damsel.base.BoundType;
import dev.vality.damsel.base.TimestampInterval;
import dev.vality.damsel.base.TimestampIntervalBound;
import dev.vality.damsel.fraudbusters.*;
import dev.vality.fraudbusters.management.converter.payment.*;
import dev.vality.swag.fraudbusters.management.api.PaymentsHistoricalDataApi;
import dev.vality.swag.fraudbusters.management.model.*;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class PaymentHistoricalDataResource implements PaymentsHistoricalDataApi {

    private final HistoricalDataServiceSrv.Iface historicalDataServiceSrv;
    private final ListToListConverter listToListConverter;
    private final PaymentToApiPaymentConverter paymentInfoToPaymentConverter;
    private final ChargebackToApiChargebackConverter chargebackInfoToChargebackConverter;
    private final RefundToApiRefundConverter refundInfoToRefundConverter;
    private final FraudPaymentInfoToFraudPaymentConverter fraudPaymentInfoToFraudPaymentConverter;
    private final FraudResultToInspectResultConverter fraudResultToInspectResultConverter;

    @SneakyThrows
    @Override
    @PreAuthorize("hasAnyRole('fraud-support', 'fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<PaymentsResponse> filterPayments(String from, String to,
                                                           String continuationId, String sortOrder,
                                                           String sortBy, Integer size,
                                                           String partyId, String shopId,
                                                           String paymentId, String status,
                                                           String email, String providerCountry,
                                                           String cardToken, String fingerprint,
                                                           String terminal, String invoiceId,
                                                           String maskedPan, String template,
                                                           String rule) {
        log.info("-> filterPaymentsInfo continuationId: {} size: {} partyId: {} shopId: {} paymentId: {} status: {} " +
                        "email: {} providerCountry: {} cardToken: {} fingerprint: {} terminal: {} " +
                        "template: {} rule: {}",
                continuationId, size, partyId, shopId, paymentId, status, email, providerCountry, cardToken,
                fingerprint, terminal, template, rule);
        var filter = createFilter(createTimestampInterval(from, to), partyId, shopId, paymentId, status, email,
                providerCountry, cardToken, fingerprint, terminal)
                .setTemplate(template)
                .setRule(rule);
        var payments = historicalDataServiceSrv.getPayments(
                filter,
                createPage(continuationId, size),
                createSort(sortOrder, sortBy));
        var paymentsResponse = new PaymentsResponse()
                .result(listToListConverter.convert(payments.getData().getPayments(), paymentInfoToPaymentConverter))
                .continuationId(payments.getContinuationId());
        log.info("<- filterPaymentsInfo paymentsResponse: {}", paymentsResponse);
        return ResponseEntity.ok(paymentsResponse);
    }

    @SneakyThrows
    @Override
    @PreAuthorize("hasAnyRole('fraud-support', 'fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<ChargebacksResponse> filterChargebacks(String from, String to,
                                                                 String continuationId, String sortOrder,
                                                                 String sortBy, Integer size,
                                                                 String partyId, String shopId,
                                                                 String paymentId, String status,
                                                                 String email, String providerCountry,
                                                                 String cardToken, String fingerprint,
                                                                 String terminal, String invoiceId,
                                                                 String maskedPan) {
        log.info("-> filterChargebacks continuationId: {} size: {} partyId: {} shopId: {} paymentId: {} status: {} " +
                        "email: {} providerCountry: {} cardToken: {} fingerprint: {} terminal: {}",
                continuationId, size, partyId, shopId, paymentId, status, email, providerCountry, cardToken,
                fingerprint,
                terminal);
        var chargebacks = historicalDataServiceSrv.getChargebacks(
                createFilter(createTimestampInterval(from, to), partyId, shopId, paymentId, status, email,
                        providerCountry, cardToken, fingerprint, terminal),
                createPage(continuationId, size),
                createSort(sortOrder, sortBy));
        var chargebacksResponse = new ChargebacksResponse()
                .result(listToListConverter
                        .convert(chargebacks.getData().getChargebacks(), chargebackInfoToChargebackConverter))
                .continuationId(chargebacks.getContinuationId());
        log.info("<- filterChargebacks chargebacksResponse: {}", chargebacksResponse);
        return ResponseEntity.ok(chargebacksResponse);
    }

    @SneakyThrows
    @Override
    public ResponseEntity<FraudPaymentsResponse> filterFraudPayments(String from,
                                                                     String to,
                                                                     String continuationId,
                                                                     String sortOrder, String sortBy,
                                                                     Integer size, String partyId,
                                                                     String shopId, String paymentId,
                                                                     String status, String email,
                                                                     String providerCountry,
                                                                     String cardToken, String fingerprint,
                                                                     String terminal, String invoiceId,
                                                                     String maskedPan) {
        log.info("-> filterFraudPayments continuationId: {} size: {} partyId: {} shopId: {} paymentId: {} status: {} " +
                        "email: {} providerCountry: {} cardToken: {} fingerprint: {} terminal: {}",
                continuationId, size, partyId, shopId, paymentId, status, email, providerCountry, cardToken,
                fingerprint,
                terminal);
        var fraudPayments = historicalDataServiceSrv.getFraudPayments(
                createFilter(createTimestampInterval(from, to), partyId, shopId, paymentId, status, email,
                        providerCountry, cardToken, fingerprint, terminal),
                createPage(continuationId, size),
                createSort(sortOrder, sortBy));
        var fraudPaymentsResponse = new FraudPaymentsResponse()
                .result(listToListConverter
                        .convert(fraudPayments.getData().getFraudPayments(), fraudPaymentInfoToFraudPaymentConverter))
                .continuationId(fraudPayments.getContinuationId());
        log.info("<- filterFraudPayments fraudPaymentsResponse: {}", fraudPaymentsResponse);
        return ResponseEntity.ok(fraudPaymentsResponse);
    }

    @SneakyThrows
    @Override
    public ResponseEntity<InspectResultsResponse> filterInspectResults(String from,
                                                                       String to,
                                                                       String continuationId,
                                                                       String sortOrder, String sortBy,
                                                                       Integer size, String partyId,
                                                                       String shopId, String paymentId,
                                                                       String status, String email,
                                                                       String providerCountry,
                                                                       String cardToken,
                                                                       String fingerprint,
                                                                       String terminal, String invoiceId,
                                                                       String maskedPan, String template,
                                                                       String rule) {
        log.info(
                "-> filterInspectResults continuationId: {} size: {} partyId: {} shopId: {} paymentId: {} status: {} " +
                        "email: {} providerCountry: {} cardToken: {} fingerprint: {} terminal: {} " +
                        "template: {} rule: {}",
                continuationId, size, partyId, shopId, paymentId, status, email, providerCountry, cardToken,
                fingerprint, terminal, template, rule);
        var filter = createFilter(createTimestampInterval(from, to), partyId, shopId, paymentId, status, email,
                providerCountry, cardToken, fingerprint, terminal)
                .setTemplate(template)
                .setRule(rule);
        var fraudResults = historicalDataServiceSrv.getFraudResults(
                filter,
                createPage(continuationId, size),
                createSort(sortOrder, sortBy));
        var inspectResultsResponse = new InspectResultsResponse()
                .result(listToListConverter
                        .convert(fraudResults.getData().getFraudResults(), fraudResultToInspectResultConverter))
                .continuationId(fraudResults.getContinuationId());
        log.info("<- filterInspectResults inspectResultsResponse: {}", inspectResultsResponse);
        return ResponseEntity.ok(inspectResultsResponse);
    }

    @SneakyThrows
    @Override
    public ResponseEntity<RefundsResponse> filterRefunds(String from, String to,
                                                         String continuationId, String sortOrder,
                                                         String sortBy, Integer size,
                                                         String partyId, String shopId,
                                                         String paymentId, String status,
                                                         String email, String providerCountry,
                                                         String cardToken, String fingerprint,
                                                         String terminal, String invoiceId,
                                                         String maskedPan) {
        log.info("-> filterFraudPayments continuationId: {} size: {} partyId: {} shopId: {} paymentId: {} status: {} " +
                        "email: {} providerCountry: {} cardToken: {} fingerprint: {} terminal: {}",
                continuationId, size, partyId, shopId, paymentId, status, email, providerCountry, cardToken,
                fingerprint,
                terminal);
        var fraudResults = historicalDataServiceSrv.getRefunds(
                createFilter(createTimestampInterval(from, to), partyId, shopId, paymentId, status, email,
                        providerCountry, cardToken, fingerprint, terminal),
                createPage(continuationId, size),
                createSort(sortOrder, sortBy));
        var refundsResponse = new RefundsResponse()
                .result(listToListConverter.convert(fraudResults.getData().getRefunds(), refundInfoToRefundConverter))
                .continuationId(fraudResults.getContinuationId());
        log.info("<- filterFraudPayments paymentsResponse: {}", refundsResponse);
        return ResponseEntity.ok(refundsResponse);
    }

    private TimestampInterval createTimestampInterval(String from, String to) {
        return new TimestampInterval()
                .setLowerBound(new TimestampIntervalBound()
                        .setBoundTime(from)
                        .setBoundType(BoundType.inclusive))
                .setUpperBound(new TimestampIntervalBound()
                        .setBoundTime(to)
                        .setBoundType(BoundType.inclusive));
    }

    private Sort createSort(String sortOrder, String sortBy) {
        return new Sort()
                .setField(sortBy)
                .setOrder(sortOrder != null ? SortOrder.valueOf(sortOrder) : SortOrder.DESC);
    }

    private Page createPage(String lastId, Integer size) {
        return new Page()
                .setContinuationId(lastId)
                .setSize(size);
    }

    private Filter createFilter(TimestampInterval timestampInterval, String partyId, String shopId, String paymentId,
                                String status, String email, String providerCountry, String cardToken,
                                String fingerprint, String terminal) {
        return new Filter()
                .setInterval(timestampInterval)
                .setEmail(email)
                .setCardToken(cardToken)
                .setPaymentId(paymentId)
                .setFingerprint(fingerprint)
                .setPartyId(partyId)
                .setProviderCountry(providerCountry)
                .setShopId(shopId)
                .setStatus(status)
                .setTerminal(terminal);
    }
}
