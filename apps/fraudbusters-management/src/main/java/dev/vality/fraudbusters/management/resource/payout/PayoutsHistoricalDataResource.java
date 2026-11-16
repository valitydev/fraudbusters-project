package dev.vality.fraudbusters.management.resource.payout;

import dev.vality.damsel.base.BoundType;
import dev.vality.damsel.base.TimestampInterval;
import dev.vality.damsel.base.TimestampIntervalBound;
import dev.vality.damsel.fraudbusters.Filter;
import dev.vality.damsel.fraudbusters.HistoricalDataServiceSrv;
import dev.vality.damsel.fraudbusters.Page;
import dev.vality.damsel.fraudbusters.Sort;
import dev.vality.damsel.fraudbusters.SortOrder;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.fraudbusters.management.converter.payout.WithdrawalToApiWithdrawalConverter;
import dev.vality.fraudbusters.management.utils.UserInfoService;
import dev.vality.swag.fraudbusters.management.api.PayoutsHistoricalDataApi;
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

/** Payout history; search values may be personal data, so only paging is logged. */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PayoutsHistoricalDataResource implements PayoutsHistoricalDataApi {

    private final HistoricalDataServiceSrv.Iface historicalDataServiceSrv;
    private final WithdrawalToApiWithdrawalConverter withdrawalConverter;
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
            throw new IllegalArgumentException("Unknown payout status: " + status);
        }
        Filter filter = new Filter()
                .setInterval(new TimestampInterval()
                        .setLowerBound(new TimestampIntervalBound()
                                .setBoundTime(from)
                                .setBoundType(BoundType.inclusive))
                        .setUpperBound(new TimestampIntervalBound()
                                .setBoundTime(to)
                                .setBoundType(BoundType.inclusive)))
                .setPartyId(partyId)
                .setWalletId(walletId)
                .setPaymentId(withdrawalId)
                .setStatus(status)
                .setEmail(email)
                .setProviderCountry(providerCountry)
                .setCardToken(cardToken)
                .setFingerprint(fingerprint)
                .setTerminal(terminal)
                .setMaskedPan(maskedPan)
                .setCustomerId(customerId)
                .setUserId(userId);
        Page page = new Page().setContinuationId(continuationId);
        if (size != null) {
            page.setSize(size);
        }
        var withdrawals = historicalDataServiceSrv.getWithdrawals(
                filter,
                page,
                new Sort()
                        .setField(sortBy)
                        .setOrder(sortOrder != null ? SortOrder.valueOf(sortOrder) : SortOrder.DESC));
        List<dev.vality.damsel.fraudbusters.Withdrawal> result = withdrawals.getData().isSetWithdrawals()
                ? withdrawals.getData().getWithdrawals()
                : List.of();
        log.info("<- filterWithdrawals found: {} continuationId: {}", result.size(), withdrawals.getContinuationId());
        return ResponseEntity.ok(new WithdrawalsResponse()
                .result(result.stream().map(withdrawalConverter::convert).toList())
                .continuationId(withdrawals.getContinuationId()));
    }
}
