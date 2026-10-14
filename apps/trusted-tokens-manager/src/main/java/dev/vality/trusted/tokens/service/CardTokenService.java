package dev.vality.trusted.tokens.service;

import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;

public interface CardTokenService {

    void save(CardTokenData cardTokenData, String token);

    CardTokenData get(String token);

    CardTokenData addWithdrawal(CardTokenData cardTokenData, CardTokensTransactionInfo cardTokensTransactionInfo);

    CardTokenData addPayment(CardTokenData cardTokenData, CardTokensTransactionInfo cardTokensTransactionInfo);


}
