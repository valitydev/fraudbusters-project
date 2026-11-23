package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.damsel.fraudbusters.Filter;
import dev.vality.damsel.fraudbusters.HistoricalWithdrawalCheck;
import dev.vality.damsel.fraudbusters.HistoricalDataServiceSrv;
import dev.vality.damsel.fraudbusters.Page;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fraudbusters.management.converter.payout.WithdrawalCheckToApiWithdrawalInspectResultConverter;
import dev.vality.fraudbusters.management.converter.payout.WithdrawalToApiWithdrawalConverter;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.WithdrawalsHistoricalDataApi;
import dev.vality.swag.fraudbusters.management.model.WithdrawalInspectResultsResponse;
import dev.vality.swag.fraudbusters.management.model.WithdrawalsResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

/** Withdrawal history; search values may be personal data, so only paging is logged. */
@Slf4j
@RestController
@RequiredArgsConstructor
public class WithdrawalsHistoricalDataResource implements WithdrawalsHistoricalDataApi {

    private final HistoricalDataServiceSrv.Iface historicalDataServiceSrv;
    private final WithdrawalToApiWithdrawalConverter withdrawalConverter;
    private final WithdrawalCheckToApiWithdrawalInspectResultConverter withdrawalInspectResultConverter;
    private final UserInfoService userInfoService;

    @SneakyThrows
    @Override
    @PreAuthorize("hasAnyRole('fraud-support', 'fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<WithdrawalsResponse> filterWithdrawals(String from, String to, String continuationId,
                                                                 String sortOrder, String sortBy, Integer size,
                                                                 String partyId, String walletId,
                                                                 String withdrawalId, String status, String email,
                                                                 String providerCountry, String cardToken,
                                                                 String fingerprint, String terminal,
                                                                 String maskedPan, String customerId,
                                                                 String userId) {
        log.info("-> filterWithdrawals initiator: {} continuationId: {} size: {}",
                userInfoService.getUserName(), continuationId, size);
        if (StringUtils.hasLength(status) && Arrays.stream(WithdrawalStatus.values())
                .noneMatch(known -> known.name().equals(status))) {
            throw new IllegalArgumentException("Unknown withdrawal status: " + status);
        }
        Filter filter = WithdrawalFilterFactory.forWithdrawals(from, to, partyId, walletId, withdrawalId, status,
                email, cardToken, fingerprint, maskedPan, customerId, userId, providerCountry, terminal);
        Page page = new Page().setContinuationId(continuationId);
        if (size != null) {
            page.setSize(size);
        }
        var withdrawals = historicalDataServiceSrv.getWithdrawals(
                filter,
                page,
                WithdrawalFilterFactory.buildSort(sortBy, sortOrder));
        List<dev.vality.damsel.fraudbusters.Withdrawal> result = withdrawals.getData().isSetWithdrawals()
                ? withdrawals.getData().getWithdrawals()
                : List.of();
        log.info("<- filterWithdrawals found: {} continuationId: {}", result.size(), withdrawals.getContinuationId());
        return ResponseEntity.ok(new WithdrawalsResponse()
                .result(result.stream().map(withdrawalConverter::convert).toList())
                .continuationId(withdrawals.getContinuationId()));
    }

    @SneakyThrows
    @Override
    @PreAuthorize("hasAnyRole('fraud-support', 'fraud-monitoring', 'fraud-officer')")
    public ResponseEntity<WithdrawalInspectResultsResponse> filterWithdrawalInspectResults(String from, String to,
                                                                                   String continuationId,
                                                                                   String sortOrder, String sortBy,
                                                                                   Integer size, String partyId,
                                                                                   String walletId,
                                                                                   String withdrawalId,
                                                                                   String status, String email,
                                                                                   String cardToken,
                                                                                   String fingerprint,
                                                                                   String maskedPan,
                                                                                   String template, String rule,
                                                                                   String customerId,
                                                                                   String userId) {
        log.info("-> filterWithdrawalInspectResults initiator: {} continuationId: {} size: {}",
                userInfoService.getUserName(), continuationId, size);
        Filter filter = WithdrawalFilterFactory.forInspectResults(from, to, partyId, walletId, withdrawalId, status,
                email, cardToken, fingerprint, maskedPan, customerId, userId, template, rule);
        Page page = new Page().setContinuationId(continuationId);
        if (size != null) {
            page.setSize(size);
        }
        var withdrawalChecks = historicalDataServiceSrv.getWithdrawalChecks(
                filter,
                page,
                WithdrawalFilterFactory.buildSort(sortBy, sortOrder));
        List<HistoricalWithdrawalCheck> result = withdrawalChecks.getData().isSetWithdrawalChecks()
                ? withdrawalChecks.getData().getWithdrawalChecks()
                : List.of();
        log.info("<- filterWithdrawalInspectResults found: {} continuationId: {}",
                result.size(), withdrawalChecks.getContinuationId());
        return ResponseEntity.ok(new WithdrawalInspectResultsResponse()
                .result(result.stream().map(withdrawalInspectResultConverter::convert).toList())
                .continuationId(withdrawalChecks.getContinuationId()));
    }
}
