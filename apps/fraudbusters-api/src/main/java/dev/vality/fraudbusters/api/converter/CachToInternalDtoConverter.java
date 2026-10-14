package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CurrencyRef;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class CachToInternalDtoConverter
        implements Converter<dev.vality.swag.fraudbusters.model.Cash, Cash> {

    @Override
    public Cash convert(dev.vality.swag.fraudbusters.model.Cash cash) {
        return new Cash()
                .setAmount(cash.getAmount())
                .setCurrency(new CurrencyRef()
                        .setSymbolicCode(cash.getCurrency()));
    }

}
