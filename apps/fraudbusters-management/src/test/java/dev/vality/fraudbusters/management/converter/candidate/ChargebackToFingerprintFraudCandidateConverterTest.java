package dev.vality.fraudbusters.management.converter.candidate;

import dev.vality.fraud_data_crawler.FraudDataCandidate;
import dev.vality.swag.fraudbusters.management.model.Chargeback;
import dev.vality.swag.fraudbusters.management.model.ClientInfo;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChargebackToFingerprintFraudCandidateConverterTest {

    private final ChargebackToFingerprintFraudCandidateConverter converter =
            new ChargebackToFingerprintFraudCandidateConverter();

    @Test
    void createsCandidateInListReadByFingerprintRules() {
        Chargeback chargeback = new Chargeback()
                .clientInfo(new ClientInfo().fingerprint("fp-1"));

        FraudDataCandidate candidate = converter.toCandidate(chargeback, "batch-1");

        assertEquals("FINGERPRINT", candidate.getType());
        assertEquals("fp-1", candidate.getValue());
    }
}
