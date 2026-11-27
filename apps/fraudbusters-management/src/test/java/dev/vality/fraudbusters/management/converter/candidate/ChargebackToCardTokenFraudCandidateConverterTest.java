package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.fraud_data_crawler.ListType;
import dev.vality.swag.fraudbusters.management.model.Chargeback;
import dev.vality.swag.fraudbusters.management.model.MerchantInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ChargebackToCardTokenFraudCandidateConverterTest {

    private final ChargebackToFraudCandidateConverter converter = new ChargebackToFraudCandidateConverter();

    @Test
    void createsCardTokenCandidate() {
        Chargeback chargeback = new Chargeback()
                .cardToken("tok-123")
                .merchantInfo(new MerchantInfo().partyId("party-1").shopId("shop-1"));

        FraudDataCandidate candidate = converter.toCandidate(chargeback, "batch-1");

        assertEquals("CARD_TOKEN", candidate.getType());
        assertEquals("tok-123", candidate.getValue());
        assertEquals(ListType.black, candidate.getListType());
        assertEquals("batch-1", candidate.getBatchId());
        assertEquals("party-1", candidate.getMerchantId());
        assertEquals("shop-1", candidate.getShopId());
    }

    @Test
    void skipsChargebackPaidWithoutBankCard() {
        Chargeback chargeback = new Chargeback().paymentTool("PaymentTerminal");

        assertNull(converter.toCandidate(chargeback, "batch-1"));
    }

    @Test
    void skipsChargebackWithoutPaymentTool() {
        assertNull(converter.toCandidate(new Chargeback(), "batch-1"));
    }

    @Test
    void skipsBlankCardToken() {
        assertNull(converter.toCandidate(new Chargeback().cardToken("  "), "batch-1"));
    }
}
