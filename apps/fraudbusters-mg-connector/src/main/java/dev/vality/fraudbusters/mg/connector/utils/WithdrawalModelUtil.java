package dev.vality.fraudbusters.mg.connector.utils;

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
public class WithdrawalModelUtil {

    public static ProviderInfo initProviderInfo(WithdrawalState withdrawalState, DestinationState destinationState) {
        if (!withdrawalState.isSetRoute()) {
            return null;
        }
        ProviderInfo providerInfo = new ProviderInfo();
        providerInfo.setTerminalId(String.valueOf(withdrawalState.getRoute().getTerminalId()));
        providerInfo.setProviderId(String.valueOf(withdrawalState.getRoute().getProviderId()));
        if (destinationState.getResource().isSetBankCard()
                && destinationState.getResource().getBankCard().isSetBankCard()
                && destinationState.getResource().getBankCard().getBankCard().isSetIssuerCountry()) {
            providerInfo
                    .setCountry(destinationState.getResource().getBankCard().getBankCard().getIssuerCountry().name());
        }
        return providerInfo;
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
