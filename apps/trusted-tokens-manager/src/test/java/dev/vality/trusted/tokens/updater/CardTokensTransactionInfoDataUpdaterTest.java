package dev.vality.trusted.tokens.updater;

import dev.vality.trusted.tokens.converter.TransactionToCardTokensTransactionInfoConverter;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.model.CardTokensTransactionInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.time.LocalDateTime;

import static dev.vality.trusted.tokens.utils.CardTokenDataUtils.createCardTokenData;
import static dev.vality.trusted.tokens.utils.TransactionUtils.createPayment;
import static dev.vality.trusted.tokens.utils.TransactionUtils.createPayout;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ContextConfiguration(classes = {
        CardTokenDataUpdater.class, TransactionToCardTokensTransactionInfoConverter.class,
        YearsDataUpdater.class, MonthsDataUpdater.class
})
class CardTokensTransactionInfoDataUpdaterTest {

    private final int currentYear = LocalDateTime.now().getYear();
    private final int currentMonth = LocalDateTime.now().getMonthValue();

    @Autowired
    private CardTokenDataUpdater cardTokenDataUpdater;

    @Autowired
    private TransactionToCardTokensTransactionInfoConverter transactionToCardTokensTransactionInfoConverter;

    private CardTokenData cardTokenData;
    private CardTokensTransactionInfo paymentCardTokensTransactionInfo;
    private CardTokensTransactionInfo payoutCardTokensTransactionInfo;

    @BeforeEach
    public void init() {
        cardTokenData = createCardTokenData();
        paymentCardTokensTransactionInfo = createPaymentCardToken();
        payoutCardTokensTransactionInfo = createPayoutCardToken();
    }

    @Test
    void convertPaymentTest() {
        cardTokenDataUpdater.updateCurrencyData(paymentCardTokensTransactionInfo, cardTokenData.getPayments());
        assertFalse(cardTokenData.getPayments().get("RUB").getYears()
                .containsKey(currentYear - 3));
        assertEquals(79000, cardTokenData.getPayments().get("RUB").getYears()
                .get(currentYear).getYearSum());
        assertEquals(79, cardTokenData.getPayments().get("RUB").getYears()
                .get(currentYear).getYearCount());
        assertEquals((currentMonth + 1) * 1000L, cardTokenData.getPayments().get("RUB").getYears()
                .get(currentYear)
                .getMonths().get(currentMonth).getMonthSum());
        assertEquals(currentMonth + 1, cardTokenData.getPayments().get("RUB").getYears()
                .get(currentYear)
                .getMonths().get(currentMonth).getMonthCount());
    }

    @Test
    void convertPayoutTest() {
        cardTokenDataUpdater.updateCurrencyData(payoutCardTokensTransactionInfo, cardTokenData.getPayouts());
        assertFalse(cardTokenData.getPayouts().get("RUB").getYears()
                .containsKey(currentYear - 3));
        assertEquals(79, cardTokenData.getPayouts().get("RUB").getYears()
                .get(currentYear).getYearCount());
        assertEquals(currentMonth + 1, cardTokenData.getPayouts().get("RUB").getYears()
                .get(currentYear)
                .getMonths().get(currentMonth).getMonthCount());
    }

    private CardTokensTransactionInfo createPaymentCardToken() {
        return transactionToCardTokensTransactionInfoConverter.convertPayment(createPayment());
    }

    private CardTokensTransactionInfo createPayoutCardToken() {
        return transactionToCardTokensTransactionInfoConverter.convertPayout(createPayout());
    }

}
