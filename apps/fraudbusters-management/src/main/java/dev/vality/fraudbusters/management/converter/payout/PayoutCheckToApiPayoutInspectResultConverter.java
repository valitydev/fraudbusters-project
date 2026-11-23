package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.fraudbusters.HistoricalPayoutCheck;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.ClientInfo;
import dev.vality.swag.fraudbusters.management.model.PayoutInspectResult;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PayoutCheckToApiPayoutInspectResultConverter
        implements Converter<HistoricalPayoutCheck, PayoutInspectResult> {

    @NonNull
    @Override
    public PayoutInspectResult convert(HistoricalPayoutCheck payoutCheck) {
        var payout = payoutCheck.getPayout();
        var checkResult = payoutCheck.getCheckResult();
        PayoutInspectResult result = new PayoutInspectResult()
                .payoutId(payout.getId())
                .eventTime(DateTimeUtils.toDate(payout.getEventTime()))
                .partyId(payout.getPartyId())
                .walletId(payout.getWalletId())
                .amount(payout.getCost().getAmount())
                .currency(payout.getCost().getCurrency().getSymbolicCode())
                .destinationType(PayoutInspectResult.DestinationTypeEnum.UNKNOWN)
                .resultStatus(checkResult.getConcreteCheckResult().getResultStatus().getSetField().getFieldName())
                .checkedTemplate(checkResult.getCheckedTemplate())
                .ruleChecked(checkResult.getConcreteCheckResult().getRuleChecked());
        if (payout.isSetDestinationResource() && payout.getDestinationResource().isSetBankCard()) {
            BankCard bankCard = payout.getDestinationResource().getBankCard();
            result.destinationType(PayoutInspectResult.DestinationTypeEnum.BANK_CARD)
                    .cardToken(bankCard.getToken())
                    .bin(bankCard.getBin())
                    .lastDigits(bankCard.getLastDigits())
                    .paymentSystem(bankCard.isSetPaymentSystem() ? bankCard.getPaymentSystem().getId() : null)
                    .issuerCountry(bankCard.isSetIssuerCountry() ? bankCard.getIssuerCountry().name() : null);
        }
        if (payout.isSetClientInfo()) {
            var clientInfo = payout.getClientInfo();
            result.clientInfo(new ClientInfo()
                    .customerId(clientInfo.getCustomerId())
                    .userId(clientInfo.getUserId())
                    .ip(clientInfo.getIp())
                    .fingerprint(clientInfo.getFingerprint())
                    .email(clientInfo.getEmail()));
        }
        return result;
    }
}
