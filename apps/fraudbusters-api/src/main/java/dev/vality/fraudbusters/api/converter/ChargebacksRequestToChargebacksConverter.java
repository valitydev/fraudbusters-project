package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.Chargeback;
import dev.vality.damsel.fraudbusters.ChargebackCategory;
import dev.vality.damsel.fraudbusters.ChargebackStatus;
import dev.vality.damsel.fraudbusters.PayerType;
import dev.vality.swag.fraudbusters.model.ChargebacksRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChargebacksRequestToChargebacksConverter implements Converter<ChargebacksRequest, List<Chargeback>> {

    private final CachToInternalDtoConverter cachToInternalDtoConverter;
    private final CustomerToInternalDtoConverter customerToInternalDtoConverter;
    private final PaymentResourceToPaymentToolConverter paymentResourceToPaymentToolConverter;
    private final ProviderToInternalDtoConverter providerToInternalDtoConverter;
    private final MerchantToInternalDtoConverter merchantToInternalDtoConverter;

    @Override
    public List<Chargeback> convert(ChargebacksRequest chargebacksRequest) {
        return chargebacksRequest.getChargebacks().stream()
                .map(this::mapChargeback)
                .collect(Collectors.toList());
    }

    private Chargeback mapChargeback(dev.vality.swag.fraudbusters.model.Chargeback item) {
        return new Chargeback()
                .setChargebackCode(item.getChargebackCode())
                .setCategory(ChargebackCategory.valueOf(item.getCategory().getValue()))
                .setCost(cachToInternalDtoConverter.convert(item.getCash()))
                .setId(item.getId())
                .setEventTime(item.getEventTime().toInstant().toString())
                .setClientInfo(customerToInternalDtoConverter.convert(item.getCustomer()))
                .setPayerType(PayerType.valueOf(item.getPayerType().getValue()))
                .setPaymentId(item.getPaymentId())
                .setProviderInfo(providerToInternalDtoConverter.convert(item.getProvider()))
                .setReferenceInfo(merchantToInternalDtoConverter.convert(item.getMerchant()))
                .setStatus(ChargebackStatus.valueOf(item.getStatus().getValue()))
                .setPaymentTool(paymentResourceToPaymentToolConverter.convert(item.getPaymentResource()));
    }

}
