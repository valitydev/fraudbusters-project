package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.fraudbusters.Account;
import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.damsel.fraudbusters.PayoutStatus;
import dev.vality.fraudbusters.api.exceptions.InvalidRequestException;
import dev.vality.swag.fraudbusters.model.PayoutsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PayoutsRequestToPayoutsConverter implements Converter<PayoutsRequest, List<Payout>> {

    public static final String UNKNOWN = "UNKNOWN";
    private final CachToInternalDtoConverter cachToInternalDtoConverter;
    private final ErrorToInternalDtoConverter errorToInternalDtoConverter;
    private final ProviderToInternalDtoConverter providerToInternalDtoConverter;
    private final PaymentResourceToResourceConverter paymentResourceToResourceConverter;
    private final CustomerToInternalDtoConverter customerToInternalDtoConverter;

    @Override
    public List<Payout> convert(PayoutsRequest request) {
        return request.getPayouts().stream()
                .map(this::mapPayout)
                .collect(Collectors.toList());
    }

    private Payout mapPayout(dev.vality.swag.fraudbusters.model.Payout item) {
        if (!StringUtils.hasText(item.getPartyId()) && StringUtils.hasText(item.getWalletId())) {
            throw new InvalidRequestException("payout " + item.getId() + ": walletId requires partyId");
        }
        return new Payout()
                .setCost(cachToInternalDtoConverter.convert(item.getCash()))
                .setId(item.getId())
                .setEventTime(item.getEventTime().toInstant().toString())
                .setProviderInfo(providerToInternalDtoConverter.convert(item.getProvider()))
                .setStatus(PayoutStatus.valueOf(item.getStatus().getValue()))
                .setError(errorToInternalDtoConverter.convert(item.getError()))
                .setAccount(new Account()
                        .setId(item.getAccount().getId())
                        .setCurrency(new CurrencyRef(item.getAccount().getCurrency())))
                .setPartyId(item.getPartyId())
                .setWalletId(item.getWalletId())
                .setClientInfo(item.getCustomer() == null ? null
                        : customerToInternalDtoConverter.convert(item.getCustomer()))
                .setDestinationResource(paymentResourceToResourceConverter.convert(item.getPaymentResource()));
    }

}
