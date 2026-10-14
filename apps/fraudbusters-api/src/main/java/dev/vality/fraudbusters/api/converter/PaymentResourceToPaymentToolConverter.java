package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.GenericPaymentTool;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.swag.fraudbusters.model.PaymentResource;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import static dev.vality.fraudbusters.api.constants.PaymentResourceTypeConstants.BANK_CARD;

@Component
@RequiredArgsConstructor
public class PaymentResourceToPaymentToolConverter
        implements Converter<PaymentResource, PaymentTool> {

    private final BankCardToInternalDtoConverter bankCardToInternalDtoConverter;

    @Override
    public PaymentTool convert(PaymentResource paymentResource) {
        if (BANK_CARD.equals(paymentResource.getType())) {
            var bankCard = (dev.vality.swag.fraudbusters.model.BankCard) paymentResource;
            return PaymentTool.bank_card(bankCardToInternalDtoConverter.convert(bankCard));
        }
        return PaymentTool.generic(new GenericPaymentTool()); // TODO или тут null лучше?
    }

}
