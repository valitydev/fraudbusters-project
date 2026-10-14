package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.PayerType;
import dev.vality.damsel.fraudbusters.Refund;
import dev.vality.damsel.fraudbusters.RefundStatus;
import dev.vality.swag.fraudbusters.model.RefundsRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RefundsRequestToRefundsConverter implements Converter<RefundsRequest, List<Refund>> {

    private final CachToInternalDtoConverter cachToInternalDtoConverter;
    private final CustomerToInternalDtoConverter customerToInternalDtoConverter;
    private final PaymentResourceToPaymentToolConverter paymentResourceToPaymentToolConverter;
    private final ProviderToInternalDtoConverter providerToInternalDtoConverter;
    private final MerchantToInternalDtoConverter merchantToInternalDtoConverter;
    private final ErrorToInternalDtoConverter errorToInternalDtoConverter;

    @Override
    public List<Refund> convert(RefundsRequest request) {
        return request.getRefunds().stream()
                .map(this::mapRefund)
                .collect(Collectors.toList());
    }

    private Refund mapRefund(dev.vality.swag.fraudbusters.model.Refund item) {
        return new Refund()
                .setCost(cachToInternalDtoConverter.convert(item.getCash()))
                .setId(item.getId())
                .setEventTime(item.getEventTime().toInstant().toString())
                .setClientInfo(customerToInternalDtoConverter.convert(item.getCustomer()))
                .setPayerType(PayerType.valueOf(item.getPayerType().getValue()))
                .setProviderInfo(providerToInternalDtoConverter.convert(item.getProvider()))
                .setReferenceInfo(merchantToInternalDtoConverter.convert(item.getMerchant()))
                .setStatus(RefundStatus.valueOf(item.getStatus().getValue()))
                .setError(errorToInternalDtoConverter.convert(item.getError()))
                .setPaymentId(item.getPaymentId())
                .setPaymentTool(paymentResourceToPaymentToolConverter.convert(item.getPaymentResource()));
    }

}
