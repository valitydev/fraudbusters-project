package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.fraudbusters.HistoricalWithdrawalCheck;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.ClientInfo;
import dev.vality.swag.fraudbusters.management.model.WithdrawalInspectResult;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalCheckToApiWithdrawalInspectResultConverter
        implements Converter<HistoricalWithdrawalCheck, WithdrawalInspectResult> {

    @NonNull
    @Override
    public WithdrawalInspectResult convert(HistoricalWithdrawalCheck withdrawalCheck) {
        var withdrawal = withdrawalCheck.getWithdrawal();
        var checkResult = withdrawalCheck.getCheckResult();
        WithdrawalInspectResult result = new WithdrawalInspectResult()
                .withdrawalId(withdrawal.getId())
                .eventTime(DateTimeUtils.toDate(withdrawal.getEventTime()))
                .partyId(withdrawal.getPartyId())
                .walletId(withdrawal.getWalletId())
                .amount(withdrawal.getCost().getAmount())
                .currency(withdrawal.getCost().getCurrency().getSymbolicCode())
                .destinationType(WithdrawalInspectResult.DestinationTypeEnum.UNKNOWN)
                .resultStatus(checkResult.getConcreteCheckResult().getResultStatus().getSetField().getFieldName())
                .checkedTemplate(checkResult.getCheckedTemplate())
                .ruleChecked(checkResult.getConcreteCheckResult().getRuleChecked());
        if (withdrawal.isSetDestinationResource() && withdrawal.getDestinationResource().isSetBankCard()) {
            BankCard bankCard = withdrawal.getDestinationResource().getBankCard();
            result.destinationType(WithdrawalInspectResult.DestinationTypeEnum.BANK_CARD)
                    .cardToken(bankCard.getToken())
                    .bin(bankCard.getBin())
                    .lastDigits(bankCard.getLastDigits())
                    .paymentSystem(bankCard.isSetPaymentSystem() ? bankCard.getPaymentSystem().getId() : null)
                    .issuerCountry(bankCard.isSetIssuerCountry() ? bankCard.getIssuerCountry().name() : null);
        }
        if (withdrawal.isSetClientInfo()) {
            var clientInfo = withdrawal.getClientInfo();
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
