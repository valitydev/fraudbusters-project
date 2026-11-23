package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.damsel.fraudbusters.Filter;
import dev.vality.damsel.fraudbusters.HistoricalPayoutCheck;
import dev.vality.damsel.fraudbusters.HistoricalDataServiceSrv;
import dev.vality.damsel.fraudbusters.Page;
import dev.vality.damsel.fraudbusters.PayoutStatus;
import dev.vality.fraudbusters.management.converter.payout.PayoutCheckToApiPayoutInspectResultConverter;
import dev.vality.fraudbusters.management.converter.payout.PayoutToApiPayoutConverter;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PayoutsHistoricalDataApi;
import dev.vality.swag.fraudbusters.management.model.PayoutInspectResultsResponse;
import dev.vality.swag.fraudbusters.management.model.PayoutsResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Payout history; search values may be personal data, so only paging is logged. */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PayoutsHistoricalDataResource implements PayoutsHistoricalDataApi {

    private final HistoricalDataServiceSrv.Iface historicalDataServiceSrv;
    private final PayoutToApiPayoutConverter payoutConverter;
    private final PayoutCheckToApiPayoutInspectResultConverter payoutInspectResultConverter;
    private final UserInfoService userInfoService;

    @SneakyThrows
    @Override
    @PreAuthorize("hasAnyRole('fraud-support', 'fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<PayoutsResponse> filterPayouts(String from, String to, String continuationId,
                                                                 String sortOrder, String sortBy, Integer size,
                                                                 String partyId, String walletId,
                                                                 String payoutId, String status, String email,
                                                                 String providerCountry, String cardToken,
                                                                 String fingerprint, String terminal,
                                                                 String maskedPan, String customerId,
                                                                 String userId) {
        log.info("-> filterPayouts initiator: {} continuationId: {} size: {}",
                userInfoService.getUserName(), continuationId, size);
        if (StringUtils.hasLength(status) && Arrays.stream(PayoutStatus.values())
                .noneMatch(known -> known.name().equals(status))) {
            throw new IllegalArgumentException("Unknown payout status: " + status);
        }
        Filter filter = PayoutFilterFactory.forPayouts(from, to, partyId, walletId, payoutId, status,
                email, cardToken, fingerprint, maskedPan, customerId, userId, providerCountry, terminal);
        Page page = new Page().setContinuationId(continuationId);
        if (size != null) {
            page.setSize(size);
        }
        var payouts = historicalDataServiceSrv.getPayouts(
                filter,
                page,
                PayoutFilterFactory.buildSort(sortBy, sortOrder));
        List<dev.vality.damsel.fraudbusters.Payout> result = payouts.getData().isSetPayouts()
                ? payouts.getData().getPayouts()
                : List.of();
        log.info("<- filterPayouts found: {} continuationId: {}", result.size(), payouts.getContinuationId());
        return ResponseEntity.ok(new PayoutsResponse()
                .result(result.stream().map(payoutConverter::convert).toList())
                .continuationId(payouts.getContinuationId()));
    }

    @SneakyThrows
    @Override
    @PreAuthorize("hasAnyRole('fraud-support', 'fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<PayoutInspectResultsResponse> filterPayoutInspectResults(String from, String to,
                                                                                   String continuationId,
                                                                                   String sortOrder, String sortBy,
                                                                                   Integer size, String partyId,
                                                                                   String walletId,
                                                                                   String payoutId,
                                                                                   String status, String email,
                                                                                   String cardToken,
                                                                                   String fingerprint,
                                                                                   String maskedPan,
                                                                                   String template, String rule,
                                                                                   String customerId,
                                                                                   String userId) {
        log.info("-> filterPayoutInspectResults initiator: {} continuationId: {} size: {}",
                userInfoService.getUserName(), continuationId, size);
        Filter filter = PayoutFilterFactory.forInspectResults(from, to, partyId, walletId, payoutId, status,
                email, cardToken, fingerprint, maskedPan, customerId, userId, template, rule);
        Page page = new Page().setContinuationId(continuationId);
        if (size != null) {
            page.setSize(size);
        }
        var payoutChecks = historicalDataServiceSrv.getPayoutChecks(
                filter,
                page,
                PayoutFilterFactory.buildSort(sortBy, sortOrder));
        List<HistoricalPayoutCheck> result = payoutChecks.getData().isSetPayoutChecks()
                ? payoutChecks.getData().getPayoutChecks()
                : List.of();
        log.info("<- filterPayoutInspectResults found: {} continuationId: {}",
                result.size(), payoutChecks.getContinuationId());
        return ResponseEntity.ok(new PayoutInspectResultsResponse()
                .result(result.stream().map(payoutInspectResultConverter::convert).toList())
                .continuationId(payoutChecks.getContinuationId()));
    }
}
