package dev.vality.trusted.tokens.service.impl;

import dev.vality.trusted.tokens.converter.RowConverter;
import dev.vality.trusted.tokens.dao.TokenDao;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import dev.vality.trusted.tokens.model.Row;
import dev.vality.trusted.tokens.service.CardTokenService;
import dev.vality.trusted.tokens.updater.CardTokenDataUpdater;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardTokenServiceImpl implements CardTokenService {

    private final CardTokenDataUpdater cardTokenDataUpdater;
    private final RowConverter rowConverter;
    private final TokenDao tokendao;

    @Override
    public void save(CardTokenData cardTokenData, String token) {
        Row row = rowConverter.convert(token, cardTokenData);
        tokendao.create(row);
    }

    @Override
    public CardTokenData get(String token) {
        return Optional.ofNullable(tokendao.get(token))
                .orElse(new CardTokenData());
    }

    @Override
    public CardTokenData addWithdrawal(CardTokenData cardTokenData,
                                       CardTokensTransactionInfo cardTokensTransactionInfo) {
        Map<String, CardTokenData.CurrencyData> currencyDataMap = cardTokenDataUpdater.updateCurrencyData(
                cardTokensTransactionInfo,
                Optional.ofNullable(cardTokenData.getWithdrawals())
                        .orElse(new HashMap<>()));
        cardTokenData.setWithdrawals(currencyDataMap);
        cardTokenData.setLastWithdrawalId(cardTokensTransactionInfo.getLastWithdrawalId());
        return cardTokenData;
    }

    @Override
    public CardTokenData addPayment(CardTokenData cardTokenData, CardTokensTransactionInfo cardTokensTransactionInfo) {
        Map<String, CardTokenData.CurrencyData> currencyMap = cardTokenDataUpdater.updateCurrencyData(
                cardTokensTransactionInfo,
                Optional.ofNullable(cardTokenData.getPayments())
                        .orElse(new HashMap<>()));
        cardTokenData.setPayments(currencyMap);
        cardTokenData.setLastPaymentId(cardTokensTransactionInfo.getLastPaymentId());
        return cardTokenData;
    }

}
