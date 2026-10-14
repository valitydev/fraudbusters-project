package dev.vality.trusted.tokens.updater;

import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@RequiredArgsConstructor
public class CardTokenDataUpdater {

    private final YearsDataUpdater yearsDataUpdater;

    public Map<String, CardTokenData.CurrencyData> updateCurrencyData(
            CardTokensTransactionInfo cardTokensTransactionInfo,
            Map<String, CardTokenData.CurrencyData> currencyMap) {

        currencyMap.put(cardTokensTransactionInfo.getCurrency(), CardTokenData.CurrencyData.builder()
                .years(yearsDataUpdater.updateYearsData(currencyMap, cardTokensTransactionInfo))
                .build());
        return currencyMap;
    }

}
