package dev.vality.fraudbusters.management.listener;

import dev.vality.damsel.fraudbusters.*;
import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.EventType;
import dev.vality.damsel.wb_list.IdInfo;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.PaymentId;
import dev.vality.damsel.wb_list.PayoutId;
import dev.vality.damsel.wb_list.Row;
import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.converter.CommandToGroupModelConverter;
import dev.vality.fraudbusters.management.converter.CommandToTemplateModelConverter;
import dev.vality.fraudbusters.management.converter.payout.CommandToPayoutGroupReferenceModelConverter;
import dev.vality.fraudbusters.management.converter.payout.CommandToPayoutReferenceModelConverter;
import dev.vality.fraudbusters.management.converter.payout.EventToPayoutListRecordConverter;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupDao;
import dev.vality.fraudbusters.management.dao.payout.group.PayoutGroupReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.wblist.PayoutListDao;
import dev.vality.fraudbusters.management.dao.payout.reference.PayoutReferenceDao;
import dev.vality.fraudbusters.management.dao.payout.template.PayoutTemplateDao;
import dev.vality.fraudbusters.management.listener.payout.PayoutGroupListener;
import dev.vality.fraudbusters.management.listener.payout.PayoutGroupReferenceListener;
import dev.vality.fraudbusters.management.listener.payout.PayoutReferenceListener;
import dev.vality.fraudbusters.management.listener.payout.PayoutTemplateListener;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import dev.vality.fraudbusters.management.utils.RowUtilsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.json.JsonMapper;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@PostgresqlJooqITest
@ContextConfiguration(classes = {PayoutTemplateDao.class, PayoutGroupDao.class, PayoutReferenceDao.class,
        PayoutGroupReferenceDao.class, PayoutListDao.class})
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PayoutInspectStorageTest {

    @Autowired
    private PayoutTemplateDao payoutTemplateDao;
    @Autowired
    private PayoutGroupDao payoutGroupDao;
    @Autowired
    private PayoutReferenceDao payoutReferenceDao;
    @Autowired
    private PayoutGroupReferenceDao payoutGroupReferenceDao;
    @Autowired
    private PayoutListDao payoutListDao;
    @Autowired
    private DataSource dataSource;

    private JdbcTemplate jdbc;
    private InspectStorage payment;
    private PayoutTemplateListener templateListener;
    private PayoutGroupListener groupListener;
    private PayoutReferenceListener referenceListener;
    private PayoutGroupReferenceListener groupReferenceListener;
    private WbListEventListener listListener;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("TRUNCATE af.payout_template, af.payout_group, af.payout_reference, " +
                "af.payout_group_reference, af.payout_list_record");
        payment = new InspectStorage(OperationType.payment, mock(ModelProjection.class),
                mock(ModelProjection.class), mock(ModelProjection.class), mock(ModelProjection.class),
                mock(ModelProjection.class));
        InspectStorage payout = new InspectStorage(OperationType.payout,
                ModelProjection.fromCommandDao(new CommandToTemplateModelConverter(), payoutTemplateDao),
                ModelProjection.fromCommandDao(new CommandToGroupModelConverter(), payoutGroupDao),
                ModelProjection.fromCommandDao(new CommandToPayoutReferenceModelConverter(), payoutReferenceDao),
                ModelProjection.fromCommandDao(new CommandToPayoutGroupReferenceModelConverter(),
                        payoutGroupReferenceDao),
                ModelProjection.fromListDao(new EventToPayoutListRecordConverter(
                        new RowUtilsService(JsonMapper.builder().build())), payoutListDao));
        AuditService audit = mock(AuditService.class);
        templateListener = new PayoutTemplateListener(payout, audit);
        groupListener = new PayoutGroupListener(payout, audit);
        referenceListener = new PayoutReferenceListener(payout, audit);
        groupReferenceListener = new PayoutGroupReferenceListener(payout, audit);
        listListener = new WbListEventListener(new InspectStorageRegistry(List.of(payment, payout)), audit);
    }

    @Test
    void payoutTemplateIsStoredSeparatelyAndReplayIsIdempotent() {
        var template = new Template()
                .setId("synthetic-id")
                .setOperationType(OperationType.payout)
                .setTemplate("rule:1=1->decline;".getBytes(StandardCharsets.UTF_8));
        var command = createCommand(CommandBody.template(template));
        templateListener.listen(command);
        templateListener.listen(command);
        assertEquals(1, countRows("af.payout_template"));
        var deletedTemplate = new Template()
                .setId("synthetic-id")
                .setOperationType(OperationType.payout);
        templateListener.listen(new Command()
                .setCommandType(CommandType.DELETE)
                .setCommandBody(CommandBody.template(deletedTemplate)));
        assertEquals(0, countRows("af.payout_template"));
        verifyNoInteractions(payment.templates());

        template.unsetOperationType();
        templateListener.listen(command);
        assertEquals(1, countRows("af.payout_template"));
        verifyNoInteractions(payment.templates());
    }

    @Test
    void globalAndScopedReferencesAreSeparateAndReplayIsIdempotent() {
        var reference = new TemplateReference()
                .setTemplateId("synthetic-template")
                .setOperationType(OperationType.payout)
                .setIsGlobal(true);
        var command = createCommand(CommandBody.reference(reference));
        referenceListener.listen(command);
        referenceListener.listen(command);
        reference.setIsGlobal(false)
                .setPartyId("synthetic-party")
                .setWalletId("synthetic-wallet");
        referenceListener.listen(command);
        assertEquals(2, countRows("af.payout_reference"));
        command.setCommandType(CommandType.DELETE);
        referenceListener.listen(command);
        assertEquals(1, countRows("af.payout_reference"));
    }

    @Test
    void groupIsReplacedAndRepeatedTemplateKeepsHighestPriority() {
        var group = new Group()
                .setGroupId("synthetic-group")
                .setOperationType(OperationType.payout)
                .setTemplateIds(List.of(new PriorityId(1, "old-template")));
        groupListener.listen(createCommand(CommandBody.group(group)));
        group.setTemplateIds(List.of(new PriorityId(3, "repeated"), new PriorityId(2, "repeated")));
        groupListener.listen(createCommand(CommandBody.group(group)));
        assertEquals(List.of("repeated:2"), jdbc.queryForList(
                "SELECT template_id || ':' || priority FROM af.payout_group WHERE group_id = 'synthetic-group'",
                String.class));
    }

    @Test
    void groupReferenceIsUpsertedByPartyAndWallet() {
        var reference = new GroupReference()
                .setGroupId("first")
                .setPartyId("synthetic-party")
                .setOperationType(OperationType.payout);
        groupReferenceListener.listen(createCommand(CommandBody.group_reference(reference)));
        reference.setGroupId("second");
        groupReferenceListener.listen(createCommand(CommandBody.group_reference(reference)));
        assertEquals("second", jdbc.queryForObject("SELECT group_id FROM af.payout_group_reference", String.class));
    }

    @Test
    void invalidScopesAreRejected() {
        var walletWithoutParty = new TemplateReference()
                .setTemplateId("t")
                .setWalletId("synthetic-wallet");
        assertThrows(IllegalArgumentException.class,
                () -> referenceListener.listen(createCommand(CommandBody.reference(walletWithoutParty))));
        assertEquals(0, countRows("af.payout_reference"));
    }

    @Test
    void payoutListRowsAreStoredSeparatelyFromPaymentRows() {
        var row = new Row()
                .setListType(ListType.black)
                .setListName("EMAIL")
                .setValue("synthetic@example.com")
                .setId(IdInfo.payout_id(new PayoutId()
                        .setPartyId("synthetic-party")));
        listListener.listen(createEvent(EventType.CREATED, row));
        listListener.listen(createEvent(EventType.CREATED, row));
        assertEquals(1, countRows("af.payout_list_record"));
        listListener.listen(createEvent(EventType.DELETED, row));
        assertEquals(0, countRows("af.payout_list_record"));
        verifyNoInteractions(payment.lists());

        var paymentRow = new Row(row)
                .setId(IdInfo.payment_id(new PaymentId()
                        .setPartyId("synthetic-party")));
        var paymentEvent = createEvent(EventType.CREATED, paymentRow);
        listListener.listen(paymentEvent);
        verify(payment.lists()).convertAndCreate(paymentEvent);
    }

    private int countRows(String table) {
        return jdbc.queryForObject("SELECT count(*) FROM " + table, Integer.class);
    }

    private Command createCommand(CommandBody body) {
        return new Command()
                .setCommandType(CommandType.CREATE)
                .setCommandBody(body);
    }

    private Event createEvent(EventType type, Row row) {
        return new Event()
                .setEventType(type)
                .setRow(row);
    }
}
