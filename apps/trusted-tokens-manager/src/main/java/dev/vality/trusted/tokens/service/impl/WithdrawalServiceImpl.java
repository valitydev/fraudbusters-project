package dev.vality.trusted.tokens.service.impl;

import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.trusted.tokens.converter.TransactionToCardTokensTransactionInfoConverter;
import dev.vality.trusted.tokens.exception.TransactionSavingException;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import dev.vality.trusted.tokens.service.CardTokenService;
import dev.vality.trusted.tokens.service.WithdrawalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawalServiceImpl implements WithdrawalService {

    private final TransactionToCardTokensTransactionInfoConverter transactionInfoConverter;
    private final CardTokenService cardTokenService;

    @Override
    public void saveAll(List<Withdrawal> withdrawals) {
        for (Withdrawal withdrawal : withdrawals) {
            int index = withdrawals.indexOf(withdrawal);
            try {
                String token = withdrawal.getDestinationResource().getBankCard().getToken();
                log.info("WithdrawalService start create row with withdrawalID {} status {} token {}",
                        withdrawal.getId(),
                        withdrawal.getStatus(),
                        token);
                CardTokenData cardTokenData = cardTokenService.get(token);
                String lastWithdrawalId = cardTokenData.getLastWithdrawalId();
                if (isOldCardTokenData(cardTokenData, lastWithdrawalId)) {
                    eraseWithdrawalData(cardTokenData, token);
                }
                if (Objects.isNull(lastWithdrawalId) || !Objects.equals(lastWithdrawalId, withdrawal.getId())) {
                    CardTokensTransactionInfo info = transactionInfoConverter.convertWithdrawal(withdrawal);
                    CardTokenData enrichedCardTokenData = cardTokenService.addWithdrawal(cardTokenData, info);
                    cardTokenService.save(enrichedCardTokenData, info.getToken());
                } else {
                    log.info("WithdrawalService withdrawal with id {} already exist", withdrawal.getId());
                }
            } catch (Exception e) {
                throw new TransactionSavingException(e, index);
            }
        }
    }

    private boolean isOldCardTokenData(CardTokenData cardTokenData, String lastWithdrawalId) {
        return Objects.isNull(lastWithdrawalId) && !CollectionUtils.isEmpty(cardTokenData.getPayments());
    }

    private void eraseWithdrawalData(CardTokenData cardTokenData, String token) {
        cardTokenData.setWithdrawals(null);
        cardTokenService.save(cardTokenData, token);
    }
}
