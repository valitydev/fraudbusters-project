package dev.vality.fraudbusters.mg.connector.utils;

import dev.vality.damsel.fraudbusters.ClientInfo;
import dev.vality.damsel.fraudbusters.Error;
import dev.vality.damsel.fraudbusters.ProviderInfo;
import dev.vality.fistful.base.Failure;
import dev.vality.fistful.destination.DestinationState;
import dev.vality.fistful.withdrawal.StatusChange;
import dev.vality.fistful.withdrawal.WithdrawalState;
import dev.vality.fistful.withdrawal.status.Failed;
import dev.vality.geck.serializer.kit.tbase.TBaseProcessor;
import dev.vality.geck.serializer.kit.tbase.TDomainToStringErrorHandler;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class PayoutModelUtil {

    public static ProviderInfo initProviderInfo(WithdrawalState payoutState, DestinationState destinationState) {
        if (!payoutState.isSetRoute()) {
            return null;
        }
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setTerminalId(String.valueOf(payoutState.getRoute().getTerminalId()));
        providerInfo.setProviderId(String.valueOf(payoutState.getRoute().getProviderId()));
        if (destinationState.getResource().isSetBankCard()
                && destinationState.getResource().getBankCard().isSetBankCard()
                && destinationState.getResource().getBankCard().getBankCard().isSetIssuerCountry()) {
            providerInfo
                    .setCountry(destinationState.getResource().getBankCard().getBankCard().getIssuerCountry().name());
        }
        return providerInfo;
    }

    public static ClientInfo initClientInfo(WithdrawalState payoutState) {
        if (!payoutState.isSetContactInfo() && !payoutState.isSetClientInfo()
                && !payoutState.isSetClientIdentity()) {
            return null;
        }
        ClientInfo clientInfo = new ClientInfo();
        if (payoutState.isSetContactInfo()) {
            clientInfo.setEmail(payoutState.getContactInfo().getEmail());
            clientInfo.setPhone(payoutState.getContactInfo().getPhoneNumber());
        }
        if (payoutState.isSetClientInfo()) {
            clientInfo.setIp(payoutState.getClientInfo().getIpAddress());
            clientInfo.setFingerprint(payoutState.getClientInfo().getFingerprint());
        }
        if (payoutState.isSetClientIdentity()) {
            clientInfo.setCustomerId(payoutState.getClientIdentity().getCustomerId());
            clientInfo.setUserId(payoutState.getClientIdentity().getUserId());
        }
        return clientInfo;
    }

    public static Error initError(StatusChange statusChange) {
        Error error = null;
        if (statusChange.getStatus().isSetFailed()) {
            error = new Error();
            final Failed failed = statusChange.getStatus().getFailed();
            if (failed.isSetFailure()) {
                final dev.vality.fistful.base.Failure failure = failed.getFailure();
                error.setErrorCode(parseError(failure))
                        .setErrorReason(failure.getReason());

            } else {
                error.setErrorCode("unknown error");
            }
        }
        return error;
    }

    private static String parseError(Failure failure) {
        try {
            return (new TBaseProcessor()).process(failure, new TDomainToStringErrorHandler());
        } catch (IOException e) {
            log.error("Error when parse error: {}", failure);
            return failure.getCode();
        }
    }

}
