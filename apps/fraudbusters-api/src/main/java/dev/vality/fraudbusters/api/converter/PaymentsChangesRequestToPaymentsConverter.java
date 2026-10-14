package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.PayerType;
import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.PaymentStatus;
import dev.vality.swag.fraudbusters.model.PaymentsChangesRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PaymentsChangesRequestToPaymentsConverter implements Converter<PaymentsChangesRequest, List<Payment>> {

    private final CachToInternalDtoConverter cachToInternalDtoConverter;
    private final CustomerToInternalDtoConverter customerToInternalDtoConverter;
    private final PaymentResourceToPaymentToolConverter paymentResourceToPaymentToolConverter;
    private final ProviderToInternalDtoConverter providerToInternalDtoConverter;
    private final MerchantToInternalDtoConverter merchantToInternalDtoConverter;
    private final ErrorToInternalDtoConverter errorToInternalDtoConverter;

    @Override
    public List<Payment> convert(PaymentsChangesRequest request) {
        return request.getPaymentsChanges().stream()
                .map(this::mapPayment)
                .collect(Collectors.toList());
    }

    private Payment mapPayment(dev.vality.swag.fraudbusters.model.PaymentChange item) {
        var payment = item.getPayment();
        return new Payment()
                .setCost(cachToInternalDtoConverter.convert(payment.getCash()))
                .setId(payment.getId())
                .setEventTime(payment.getCreatedAt().toInstant().toString())
                .setClientInfo(customerToInternalDtoConverter.convert(payment.getCustomer()))
                .setPayerType(PayerType.valueOf(payment.getPayerType().getValue()))
                .setProviderInfo(providerToInternalDtoConverter.convert(payment.getProvider()))
                .setReferenceInfo(merchantToInternalDtoConverter.convert(payment.getMerchant()))
                .setStatus(PaymentStatus.valueOf(item.getPaymentStatus().getValue()))
                .setError(errorToInternalDtoConverter.convert(item.getError()))
                .setPaymentTool(paymentResourceToPaymentToolConverter.convert(payment.getPaymentResource()))
                .setRecurrent(dev.vality.swag.fraudbusters.model.PayerType.RECURRENT.equals(payment.getPayerType()))
                .setMobile(dev.vality.swag.fraudbusters.model.PayerType.MOBILE.equals(payment.getPayerType()));
    }

}
