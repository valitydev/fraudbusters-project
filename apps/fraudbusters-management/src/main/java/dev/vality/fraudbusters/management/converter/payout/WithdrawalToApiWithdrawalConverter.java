package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.domain.BankCard;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.ClientInfo;
import dev.vality.swag.fraudbusters.management.model.Error;
import dev.vality.swag.fraudbusters.management.model.ProviderInfo;
import dev.vality.swag.fraudbusters.management.model.Withdrawal;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class WithdrawalToApiWithdrawalConverter
        implements Converter<dev.vality.damsel.fraudbusters.Withdrawal, Withdrawal> {

    @NonNull
    @Override
    public Withdrawal convert(dev.vality.damsel.fraudbusters.Withdrawal withdrawal) {
        Withdrawal result = new Withdrawal()
                .id(withdrawal.getId())
                .eventTime(DateTimeUtils.toDate(withdrawal.getEventTime()))
                .partyId(withdrawal.getPartyId())
                .walletId(withdrawal.getWalletId())
                .amount(withdrawal.getCost().getAmount())
                .currency(withdrawal.getCost().getCurrency().getSymbolicCode())
                .status(Withdrawal.StatusEnum.fromValue(withdrawal.getStatus().name()))
                .destinationType(Withdrawal.DestinationTypeEnum.UNKNOWN);
        if (withdrawal.isSetAccount()) {
            result.accountId(withdrawal.getAccount().getId())
                    .accountCurrency(withdrawal.getAccount().getCurrency().getSymbolicCode());
        }
        if (withdrawal.isSetDestinationResource() && withdrawal.getDestinationResource().isSetBankCard()) {
            BankCard bankCard = withdrawal.getDestinationResource().getBankCard();
            result.destinationType(Withdrawal.DestinationTypeEnum.BANK_CARD)
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
        if (withdrawal.isSetError()) {
            result.error(new Error()
                    .errorCode(withdrawal.getError().getErrorCode())
                    .errorReason(withdrawal.getError().getErrorReason()));
        }
        if (withdrawal.isSetProviderInfo()) {
            result.provider(new ProviderInfo()
                    .providerId(withdrawal.getProviderInfo().getProviderId())
                    .terminalId(withdrawal.getProviderInfo().getTerminalId())
                    .country(withdrawal.getProviderInfo().getCountry()));
        }
        return result;
    }
}
