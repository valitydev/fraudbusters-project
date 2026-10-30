package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PayoutCommandKeyTest {

    @Test
    void legacyKeysArePreservedAndPayoutTemplateCannotReplacePaymentDuringCompaction() {
        var template = new Template().setId("shared-id");
        var command = new Command().setCommandBody(CommandBody.template(template));
        assertEquals("shared-id", PayoutCommandKey.forCommand(command, "shared-id"));
        template.setOperationType(OperationType.payment);
        assertEquals("shared-id", PayoutCommandKey.forCommand(command, "shared-id"));
        template.setOperationType(OperationType.payout);
        String payout = PayoutCommandKey.forCommand(command, "shared-id");
        assertNotEquals("shared-id", payout);
        assertEquals(0, payout.charAt(0));
        command.setCommandType(CommandType.DELETE);
        assertEquals(payout, PayoutCommandKey.forCommand(command, "ignored"));
    }

    @Test
    void walletScopesAndGlobalReferencesHaveStableDistinctKeys() {
        var reference = new TemplateReference().setOperationType(OperationType.payout)
                .setTemplateId("template").setPartyId("party").setWalletId("wallet-a");
        var command = new Command().setCommandBody(CommandBody.reference(reference));
        String first = PayoutCommandKey.forCommand(command, "party");
        reference.setWalletId("wallet-b");
        assertNotEquals(first, PayoutCommandKey.forCommand(command, "party"));
        reference.setIsGlobal(true);
        String global = PayoutCommandKey.forCommand(command, "GLOBAL");
        reference.setPartyId("ignored").setWalletId("ignored");
        assertEquals(global, PayoutCommandKey.forCommand(command, "GLOBAL"));
        assertNotEquals(first, global);
    }
}
