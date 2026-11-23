package dev.vality.trusted.tokens.service.impl;

import dev.vality.damsel.fraudbusters.Payout;
import dev.vality.trusted.tokens.converter.TransactionToCardTokensTransactionInfoConverter;
import dev.vality.trusted.tokens.exception.TransactionSavingException;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import dev.vality.trusted.tokens.service.CardTokenService;
import dev.vality.trusted.tokens.service.PayoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PayoutServiceImpl implements PayoutService {

    private final TransactionToCardTokensTransactionInfoConverter transactionInfoConverter;
    private final CardTokenService cardTokenService;

    @Override
    public void saveAll(List<Payout> payouts) {
        for (Payout payout : payouts) {
            int index = payouts.indexOf(payout);
            try {
                String token = payout.getDestinationResource().getBankCard().getToken();
                log.info("PayoutService start create row with payoutID {} status {} token {}",
                        payout.getId(),
                        payout.getStatus(),
                        token);
                CardTokenData cardTokenData = cardTokenService.get(token);
                String lastPayoutId = cardTokenData.getLastPayoutId();
                if (isOldCardTokenData(cardTokenData, lastPayoutId)) {
                    erasePayoutData(cardTokenData, token);
                }
                if (Objects.isNull(lastPayoutId) || !Objects.equals(lastPayoutId, payout.getId())) {
                    CardTokensTransactionInfo info = transactionInfoConverter.convertPayout(payout);
                    CardTokenData enrichedCardTokenData = cardTokenService.addPayout(cardTokenData, info);
                    cardTokenService.save(enrichedCardTokenData, info.getToken());
                } else {
                    log.info("PayoutService payout with id {} already exist", payout.getId());
                }
            } catch (Exception e) {
                throw new TransactionSavingException(e, index);
            }
        }
    }

    private boolean isOldCardTokenData(CardTokenData cardTokenData, String lastPayoutId) {
        return Objects.isNull(lastPayoutId) && !CollectionUtils.isEmpty(cardTokenData.getPayments());
    }

    private void erasePayoutData(CardTokenData cardTokenData, String token) {
        cardTokenData.setPayouts(null);
        cardTokenService.save(cardTokenData, token);
    }
}
