package dev.vality.fraudo;

import dev.vality.fraudo.constant.ResultStatus;
import dev.vality.fraudo.test.model.PaymentModel;
import dev.vality.fraudo.utils.ResultUtils;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class ClientIdentityTest extends AbstractPaymentTest {

    @Test
    void identityFunctionsWorkInBothVisitorsAndPreserveCase() throws Exception {
        var parse = getParseContext(new ByteArrayInputStream((
                "rule: customerId() = \"synthetic-customer\" AND in(userId(), \"Synthetic-User\") -> accept;")
                .getBytes(StandardCharsets.UTF_8)));
        PaymentModel model = new PaymentModel();
        model.setCustomerId("synthetic-customer");
        model.setUserId("Synthetic-User");
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(invoke(parse, model))
                .orElseThrow().getResultStatus());
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(invokeFullVisitor(parse, model))
                .orElseThrow().getResultStatus());
        model.setUserId("synthetic-user");
        assertTrue(ResultUtils.findFirstNotNotifyStatus(invoke(parse, model)).isEmpty());
    }

    @Test
    void oldPaymentsWithoutIdentityRemainSupported() throws Exception {
        var parse = getParseContext(new ByteArrayInputStream((
                "rule: customerId() = \"UNKNOWN\" AND userId() = \"UNKNOWN\" -> accept;")
                .getBytes(StandardCharsets.UTF_8)));
        assertEquals(ResultStatus.ACCEPT, ResultUtils.findFirstNotNotifyStatus(invoke(parse, new PaymentModel()))
                .orElseThrow().getResultStatus());
    }
}
