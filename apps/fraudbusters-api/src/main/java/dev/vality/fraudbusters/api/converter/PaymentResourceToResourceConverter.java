package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.fraudbusters.GenericPaymentTool;
import dev.vality.damsel.fraudbusters.Resource;
import dev.vality.swag.fraudbusters.model.PaymentResource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static dev.vality.fraudbusters.api.constants.PaymentResourceTypeConstants.BANK_CARD;

@Component
@RequiredArgsConstructor
public class PaymentResourceToResourceConverter
        implements Converter<PaymentResource, Resource> {

    private final BankCardToInternalDtoConverter bankCardToInternalDtoConverter;

    @Override
    public Resource convert(PaymentResource paymentResource) {
        if (BANK_CARD.equals(paymentResource.getType())) {
            var bankCard = (dev.vality.swag.fraudbusters.model.BankCard) paymentResource;
            return Resource.bank_card(bankCardToInternalDtoConverter.convert(bankCard));
        }
        return Resource.generic(new GenericPaymentTool()); // TODO или тут null лучше?
    }

}
