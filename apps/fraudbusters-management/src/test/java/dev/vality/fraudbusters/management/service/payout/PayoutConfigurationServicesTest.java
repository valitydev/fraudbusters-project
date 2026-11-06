package dev.vality.fraudbusters.management.service.payout;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.OperationType;
import dev.vality.damsel.fraudbusters.PaymentServiceSrv;
import dev.vality.damsel.fraudbusters.TemplateValidateError;
import dev.vality.damsel.fraudbusters.ValidateTemplateResponse;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.converter.TemplateModelToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.GroupModelToGroupConverter;
import dev.vality.fraudbusters.management.converter.payment.GroupToCommandConverter;
import dev.vality.fraudbusters.management.converter.payment.TemplateModelToTemplateConverter;
import dev.vality.fraudbusters.management.converter.payment.TemplateToCommandConverter;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.reference.PayoutReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.template.PayoutTemplateDao;
import dev.vality.fraudbusters.management.dao.payout.wblist.PayoutListDao;
import dev.vality.fraudbusters.management.domain.payout.PayoutReferenceModel;
import dev.vality.fraudbusters.management.exception.NotFoundException;
import dev.vality.fraudbusters.management.service.CommandSender;
import dev.vality.fraudbusters.management.service.WbListCommandService;
import dev.vality.fraudbusters.management.utils.CountInfoApiUtils;
import dev.vality.swag.fraudbusters.management.model.CountInfo;
import dev.vality.swag.fraudbusters.management.model.Group;
import dev.vality.swag.fraudbusters.management.model.ListType;
import dev.vality.swag.fraudbusters.management.model.PayoutCountInfo;
import dev.vality.swag.fraudbusters.management.model.PayoutGroupReference;
import dev.vality.swag.fraudbusters.management.model.PayoutListRecord;
import dev.vality.swag.fraudbusters.management.model.PayoutReference;
import dev.vality.swag.fraudbusters.management.model.PayoutRowListRequest;
import dev.vality.swag.fraudbusters.management.model.PriorityId;
import dev.vality.swag.fraudbusters.management.model.Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class PayoutConfigurationServicesTest {

    private static final String USER = "synthetic-user";

    private final CommandSender commandSender = mock(CommandSender.class);
    private final PayoutCommandService commandService = new PayoutCommandService(commandSender);
    private final PaymentServiceSrv.Iface fraudbusters = mock(PaymentServiceSrv.Iface.class);
    private final PayoutReferenceDao referenceDao = mock(PayoutReferenceDao.class);
    private final WbListCommandService wbListCommandService = mock(WbListCommandService.class);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(commandService, "templateTopic", "payout_template");
        ReflectionTestUtils.setField(commandService, "referenceTopic", "payout_template_reference");
        ReflectionTestUtils.setField(commandService, "groupTopic", "payout_group_list");
        ReflectionTestUtils.setField(commandService, "groupReferenceTopic", "payout_group_reference");
        when(commandSender.send(anyString(), any(), anyString())).thenAnswer(call -> call.getArgument(2));
    }

    @Test
    void templateIsValidatedAsPayoutAndSentOnlyWhenValid() throws Exception {
        var service = templatesService();
        when(fraudbusters.validateCompilationTemplate(any())).thenReturn(new ValidateTemplateResponse()
                .setErrors(List.of(new TemplateValidateError()
                        .setId("payout-template")
                        .setReason(List.of("field shop_id is not supported in countPayouts for payouts")))));

        var rejected = service.createTemplate(new Template()
                .id("payout-template")
                .template("rule: countPayouts(\"shop_id\", 24) > 1 -> decline;"), USER);

        assertEquals(List.of("field shop_id is not supported in countPayouts for payouts"), rejected.getErrors());
        assertNull(rejected.getId());
        verify(commandSender, never()).send(anyString(), any(), anyString());
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<dev.vality.damsel.fraudbusters.Template>> validated = ArgumentCaptor.forClass(List.class);
        verify(fraudbusters).validateCompilationTemplate(validated.capture());
        assertEquals(OperationType.payout, validated.getValue().get(0).getOperationType());

        when(fraudbusters.validateCompilationTemplate(any())).thenReturn(new ValidateTemplateResponse()
                .setErrors(List.of()));
        var created = service.createTemplate(new Template()
                .id("payout-template")
                .template("rule: 1 = 1 -> accept;"), USER);

        assertEquals("payout-template", created.getId());
        Command sent = sent("payout_template", "payout-template");
        assertEquals(CommandType.CREATE, sent.getCommandType());
        assertEquals(USER, sent.getUserInfo().getUserId());

        service.removeTemplate("payout-template", USER);
        assertEquals(CommandType.DELETE, lastSent("payout_template").getCommandType());
    }

    @Test
    void referencesAreKeyedByScopeAndInvalidOnesAreRejectedBeforeSending() {
        var service = new PayoutReferencesService(referenceDao, commandService);

        List<String> keys = service.insertReferences(List.of(
                new PayoutReference()
                        .templateId("global")
                        .isGlobal(true),
                new PayoutReference()
                        .templateId("party")
                        .partyId("party-1"),
                new PayoutReference()
                        .templateId("wallet")
                        .partyId("party-1")
                        .walletId("wallet-1")), USER);

        assertEquals(List.of("GLOBAL", "party:7:party-1", "party:7:party-1:wallet:8:wallet-1"), keys);
        var wallet = lastSent("payout_template_reference").getCommandBody().getReference();
        assertEquals("party-1", wallet.getPartyId());
        assertEquals("wallet-1", wallet.getWalletId());
        assertFalse(wallet.isSetShopId());

        for (PayoutReference invalid : List.of(
                new PayoutReference()
                        .templateId("t")
                        .walletId("wallet-1")
                        .partyId(""),
                new PayoutReference()
                        .templateId("t")
                        .isGlobal(true)
                        .partyId("party-1"),
                new PayoutReference()
                        .templateId("t"),
                new PayoutReference()
                        .partyId("party-1"))) {
            var batch = List.of(new PayoutReference()
                    .templateId("valid")
                    .partyId("party-2"), invalid);
            assertThrows(IllegalArgumentException.class, () -> service.insertReferences(batch, USER));
        }
        verify(commandSender, never()).send(anyString(), any(), eq("party:7:party-2"));
    }

    @Test
    void removingAReferenceSendsTheStoredTemplateOrFails() {
        var stored = new PayoutReferenceModel();
        stored.setTemplateId("wallet-template");
        stored.setPartyId("party-1");
        stored.setWalletId("wallet-1");
        stored.setIsGlobal(false);
        when(referenceDao.getByScope("party-1", "wallet-1", false)).thenReturn(stored);
        var service = new PayoutReferencesService(referenceDao, commandService);

        assertEquals("party:7:party-1:wallet:8:wallet-1", service.removeReference("party-1", "wallet-1", false, USER));
        Command sent = lastSent("payout_template_reference");
        assertEquals(CommandType.DELETE, sent.getCommandType());
        assertEquals("wallet-template", sent.getCommandBody().getReference().getTemplateId());

        assertThrows(NotFoundException.class, () -> service.removeReference("party-1", null, false, USER));
        when(referenceDao.getByScope("", "", true)).thenReturn(null);
        assertThrows(NotFoundException.class, () -> service.removeReference("ignored", null, true, USER));
    }

    @Test
    void groupsAndGroupReferencesGoToTheirTopics() {
        var service = new PayoutGroupsService(mock(PayoutGroupDao.class), mock(PayoutGroupReferenceDao.class),
                commandService, new GroupToCommandConverter(), mock(GroupModelToGroupConverter.class));

        service.insertGroup(new Group()
                .groupId("group-1")
                .priorityTemplates(List.of(new PriorityId()
                        .id("template-1")
                        .priority(1L))), USER);
        var group = sent("payout_group_list", "group-1");
        assertEquals("template-1", group.getCommandBody().getGroup().getTemplateIds().get(0).getId());

        assertEquals(List.of("GLOBAL", "party:7:party-1:wallet:8:wallet-1"), service.insertGroupReferences(
                "group-1", List.of(new PayoutGroupReference(), new PayoutGroupReference()
                        .partyId("party-1")
                        .walletId("wallet-1")), USER));
        var reference = lastSent("payout_group_reference").getCommandBody().getGroupReference();
        assertEquals("group-1", reference.getGroupId());
        assertEquals("wallet-1", reference.getWalletId());

        service.removeGroupReference("group-1", "party-1", null, USER);
        assertEquals(CommandType.DELETE, lastSent("payout_group_reference").getCommandType());
        assertThrows(IllegalArgumentException.class, () -> service.insertGroupReferences("group-1",
                List.of(new PayoutGroupReference()
                        .walletId("wallet-1")), USER));
        assertThrows(IllegalArgumentException.class, () -> service.insertGroup(new Group(), USER));
    }

    @Test
    void listRowsUsePayoutIdentityAndGreyRowsNeedACount() {
        var service = new PayoutListsService(mock(PayoutListDao.class), wbListCommandService,
                mock(CountInfoApiUtils.class));
        ReflectionTestUtils.setField(service, "availableListNames", List.of("EMAIL", "CARD_TOKEN"));
        when(wbListCommandService.sendCommandSync(any(), any(), any(), anyString())).thenReturn("message-id");

        service.insertRows(new PayoutRowListRequest()
                .listType(ListType.BLACK)
                .records(List.of(record("EMAIL", "a@example.com", null, null, null),
                        record("CARD_TOKEN", "token-1", "party-1", "wallet-1", null))), USER);
        ArgumentCaptor<Row> rows = ArgumentCaptor.forClass(Row.class);
        verify(wbListCommandService, org.mockito.Mockito.times(2)).sendCommandSync(rows.capture(),
                eq(dev.vality.damsel.wb_list.ListType.black), eq(dev.vality.damsel.wb_list.Command.CREATE),
                eq(USER));
        assertTrue(rows.getAllValues().get(0).getId().isSetPayoutId());
        assertFalse(rows.getAllValues().get(0).getId().getPayoutId().isSetPartyId());
        assertEquals("wallet-1", rows.getAllValues().get(1).getId().getPayoutId().getWalletId());

        service.insertRows(new PayoutRowListRequest()
                .listType(ListType.GREY)
                .records(List.of(record("CARD_TOKEN", "token-2", "party-1", null, new CountInfo()
                        .count(5L)
                        .endCountTime(LocalDateTime.parse("2026-02-01T00:00:00"))))), USER);
        verify(wbListCommandService).sendCommandSync(rows.capture(), eq(dev.vality.damsel.wb_list.ListType.grey),
                any(), eq(USER));
        var countInfo = rows.getValue().getRowInfo().getCountInfo();
        assertEquals(5, countInfo.getCount());
        assertEquals("2026-02-01T00:00:00Z", countInfo.getTimeToLive());
        assertFalse(countInfo.isSetStartCountTime());

        org.mockito.Mockito.clearInvocations(wbListCommandService);
        for (PayoutCountInfo invalid : List.of(record("SHOP_ID", "shop", null, null, null),
                record("EMAIL", " ", null, null, null),
                record("EMAIL", "a@example.com", null, "wallet-1", null))) {
            var request = new PayoutRowListRequest()
                    .listType(ListType.BLACK)
                    .records(List.of(record("EMAIL", "b@example.com", null, null, null), invalid));
            assertThrows(IllegalArgumentException.class, () -> service.insertRows(request, USER));
        }
        var greyWithoutCount = new PayoutRowListRequest()
                .listType(ListType.GREY)
                .records(List.of(record("EMAIL", "c@example.com", null, null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.insertRows(greyWithoutCount, USER));
        verifyNoInteractions(wbListCommandService);
    }

    @Test
    void namedListsTakeAnyNameWhileOtherListsTakeFieldNames() {
        var service = new PayoutListsService(mock(PayoutListDao.class), wbListCommandService,
                mock(CountInfoApiUtils.class));
        ReflectionTestUtils.setField(service, "availableListNames", List.of("EMAIL"));
        var named = new PayoutRowListRequest()
                .listType(ListType.NAMING)
                .records(List.of(record("vip_wallets", "wallet-1", "party-1", null, null)));

        service.insertRows(named, USER);

        ArgumentCaptor<Row> row = ArgumentCaptor.forClass(Row.class);
        verify(wbListCommandService).sendCommandSync(row.capture(), eq(dev.vality.damsel.wb_list.ListType.naming),
                eq(dev.vality.damsel.wb_list.Command.CREATE), eq(USER));
        assertEquals("vip_wallets", row.getValue().getListName());
        var white = new PayoutRowListRequest()
                .listType(ListType.WHITE)
                .records(List.of(record("vip_wallets", "wallet-1", "party-1", null, null)));
        assertThrows(IllegalArgumentException.class, () -> service.insertRows(white, USER));
    }

    private PayoutTemplatesService templatesService() {
        return new PayoutTemplatesService(mock(PayoutTemplateDao.class), commandService, fraudbusters,
                new TemplateToCommandConverter(), new TemplateModelToCommandConverter(),
                mock(TemplateModelToTemplateConverter.class));
    }

    private static PayoutCountInfo record(String listName, String value, String partyId, String walletId,
                                          CountInfo countInfo) {
        return new PayoutCountInfo()
                .listRecord(new PayoutListRecord()
                        .listName(listName)
                        .value(value)
                        .partyId(partyId)
                        .walletId(walletId))
                .countInfo(countInfo);
    }

    private Command sent(String topic, String key) {
        ArgumentCaptor<Command> command = ArgumentCaptor.forClass(Command.class);
        verify(commandSender, org.mockito.Mockito.atLeastOnce()).send(eq(topic), command.capture(), eq(key));
        return command.getValue();
    }

    private Command lastSent(String topic) {
        ArgumentCaptor<Command> command = ArgumentCaptor.forClass(Command.class);
        verify(commandSender, org.mockito.Mockito.atLeastOnce()).send(eq(topic), command.capture(), anyString());
        return command.getValue();
    }
}
