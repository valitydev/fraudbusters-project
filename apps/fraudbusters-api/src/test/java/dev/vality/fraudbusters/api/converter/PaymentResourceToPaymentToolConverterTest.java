package dev.vality.fraudbusters.api.converter;

import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.fraudbusters.api.utils.ApiBeanGenerator;
import org.apache.thrift.TException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class PaymentResourceToPaymentToolConverterTest {

    @Test
    void convert() throws TException {
        PaymentResourceToPaymentToolConverter converter =
                new PaymentResourceToPaymentToolConverter(new BankCardToInternalDtoConverter());
        PaymentTool paymentTool = converter.convert(ApiBeanGenerator.initBankCard());

        assertNotNull(paymentTool);

        BankCard bankCard = paymentTool.getBankCard();
        bankCard.validate();

        assertEquals(ApiBeanGenerator.BANK_NAME, bankCard.bank_name);
        assertEquals(ApiBeanGenerator.BIN, bankCard.bin);
        assertEquals(ApiBeanGenerator.CARD_TOKEN, bankCard.token);
        assertEquals(ApiBeanGenerator.LAST_DIGITS, bankCard.last_digits);
    }
}
