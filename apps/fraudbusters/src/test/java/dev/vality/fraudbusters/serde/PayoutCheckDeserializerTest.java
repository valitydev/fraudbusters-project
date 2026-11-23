package dev.vality.fraudbusters.serde;

import dev.vality.fraudbusters.domain.CheckedResultModel;
import dev.vality.fraudbusters.domain.ConcreteResultModel;
import dev.vality.fraudbusters.domain.PayoutResult;
import dev.vality.fraudbusters.fraud.model.PayoutModel;
import dev.vality.fraudo.constant.ResultStatus;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class PayoutCheckDeserializerTest {

    @Test
    void readsWhatTheInspectorSends() {
        PayoutModel model = new PayoutModel();
        model.setPayoutId("payout");
        model.setPartyId("party");
        model.setWalletId("wallet");
        model.setAmount(1000L);
        model.setCurrency("RUB");
        model.setTimestamp(1760000000000L);
        model.setCardToken("token");
        model.setUserId("user");
        CheckedResultModel result = new CheckedResultModel();
        result.setCheckedTemplate("template");
        result.setResultModel(new ConcreteResultModel(ResultStatus.DECLINE, "card_velocity", null));
        PayoutResult expected = new PayoutResult(model, result);

        try (var serializer = new JacksonJsonSerializer<PayoutResult>()) {
            byte[] data = serializer.serialize("payout_result", expected);

            assertEquals(expected, new PayoutCheckDeserializer().deserialize("payout_result", data));
        }
    }

    @Test
    void skipsUnreadableMessages() {
        assertNull(new PayoutCheckDeserializer().deserialize(
                "payout_result", "invalid".getBytes(StandardCharsets.UTF_8)));
    }
}
