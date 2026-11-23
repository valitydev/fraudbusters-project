package dev.vality.fraudbusters.management.converter.payout;

import dev.vality.damsel.domain.BankCard;
import dev.vality.fraudbusters.management.utils.DateTimeUtils;
import dev.vality.swag.fraudbusters.management.model.ClientInfo;
import dev.vality.swag.fraudbusters.management.model.Error;
import dev.vality.swag.fraudbusters.management.model.ProviderInfo;
import dev.vality.swag.fraudbusters.management.model.Payout;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

@Component
public class PayoutToApiPayoutConverter
        implements Converter<dev.vality.damsel.fraudbusters.Payout, Payout> {

    @NonNull
    @Override
    public Payout convert(dev.vality.damsel.fraudbusters.Payout payout) {
        Payout result = new Payout()
                .id(payout.getId())
                .eventTime(DateTimeUtils.toDate(payout.getEventTime()))
                .partyId(payout.getPartyId())
                .walletId(payout.getWalletId())
                .amount(payout.getCost().getAmount())
                .currency(payout.getCost().getCurrency().getSymbolicCode())
                .status(Payout.StatusEnum.fromValue(payout.getStatus().name()))
                .destinationType(Payout.DestinationTypeEnum.UNKNOWN);
        if (payout.isSetAccount()) {
            result.accountId(payout.getAccount().getId())
                    .accountCurrency(payout.getAccount().getCurrency().getSymbolicCode());
        }
        if (payout.isSetDestinationResource() && payout.getDestinationResource().isSetBankCard()) {
            BankCard bankCard = payout.getDestinationResource().getBankCard();
            result.destinationType(Payout.DestinationTypeEnum.BANK_CARD)
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
        if (payout.isSetError()) {
            result.error(new Error()
                    .errorCode(payout.getError().getErrorCode())
                    .errorReason(payout.getError().getErrorReason()));
        }
        if (payout.isSetProviderInfo()) {
            result.provider(new ProviderInfo()
                    .providerId(payout.getProviderInfo().getProviderId())
                    .terminalId(payout.getProviderInfo().getTerminalId())
                    .country(payout.getProviderInfo().getCountry()));
        }
        return result;
    }
}
