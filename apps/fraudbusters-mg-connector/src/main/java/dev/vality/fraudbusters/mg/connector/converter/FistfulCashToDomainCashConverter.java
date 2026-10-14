package dev.vality.fraudbusters.mg.connector.converter;

import dev.vality.damsel.domain.Cash;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FistfulCashToDomainCashConverter implements Converter<dev.vality.fistful.base.Cash, Cash> {

    private final FistfulCurrencyToDomainCurrencyConverter convertCurrency;

    @Override
    public Cash convert(dev.vality.fistful.base.Cash cash) {
        final dev.vality.fistful.base.CurrencyRef currency = cash.getCurrency();
        return new Cash()
                .setAmount(cash.getAmount())
                .setCurrency(convertCurrency.convert(currency));
    }

}
