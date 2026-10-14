package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.CountryCode;
import dev.vality.damsel.domain.PaymentSystemRef;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class BankCardToInternalDtoConverter
        implements Converter<dev.vality.swag.fraudbusters.model.BankCard, BankCard> {

    @Override
    public BankCard convert(dev.vality.swag.fraudbusters.model.BankCard bankCard) {
        return new BankCard()
                .setToken(bankCard.getCardToken())
                .setBin(bankCard.getBin())
                .setLastDigits(bankCard.getLastDigits())
                .setBankName(bankCard.getBankName())
                .setIssuerCountry(CountryCode.valueOf(bankCard.getCountryCode()))
                .setPaymentSystem(new PaymentSystemRef()
                        .setId(bankCard.getPaymentSystem())
                );
    }

}
