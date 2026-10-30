package dev.vality.fraudbusters.management.dao.payout;

import dev.vality.damsel.fraudbusters.*;
import dev.vality.fraudbusters.management.config.PostgresqlJooqITest;
import dev.vality.fraudbusters.management.utils.RowUtilsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@PostgresqlJooqITest
@ContextConfiguration(classes = PayoutConfigurationDao.class)
@Transactional(propagation = Propagation.NOT_SUPPORTED)
class PayoutConfigurationDaoTest {

    @Autowired
    private PayoutConfigurationDao dao;
    @Autowired
    private DataSource dataSource;
    @MockitoBean
    private RowUtilsService rowUtilsService;
    private JdbcTemplate jdbc;

    @BeforeEach
    void setUp() {
        jdbc = new JdbcTemplate(dataSource);
        jdbc.execute("TRUNCATE af.payout_template, af.payout_group, af.payout_reference, af.payout_group_reference");
        jdbc.update("DELETE FROM af.f_template WHERE id = ?", "synthetic-id");
        jdbc.update("INSERT INTO af.f_template(id, template) VALUES (?, ?)", "synthetic-id", "payment-template");
    }

    @Test
    void payoutUpdatesAndDeletesDoNotTouchPaymentConfiguration() {
        var template = new Template().setId("synthetic-id").setOperationType(OperationType.payout)
                .setTemplate("rule:1=1->decline;".getBytes(StandardCharsets.UTF_8));
        var command = create(CommandBody.template(template));
        assertTrue(dao.apply(command));
        assertTrue(dao.apply(command));
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM af.payout_template", Integer.class));
        command.setCommandType(CommandType.DELETE);
        dao.apply(command);
        assertEquals(0, jdbc.queryForObject("SELECT count(*) FROM af.payout_template", Integer.class));
        assertEquals("payment-template", jdbc.queryForObject(
                "SELECT template FROM af.f_template WHERE id = 'synthetic-id'", String.class));
        template.unsetOperationType();
        assertFalse(dao.apply(command));
    }

    @Test
    void absentScopeAndExplicitGlobalReferenceRemainSeparateAndReplayIsIdempotent() {
        var reference = new TemplateReference().setTemplateId("synthetic-template")
                .setOperationType(OperationType.payout).setIsGlobal(true);
        var command = create(CommandBody.reference(reference));
        dao.apply(command);
        dao.apply(command);
        reference.setIsGlobal(false).setPartyId("synthetic-party").setWalletId("synthetic-wallet");
        dao.apply(command);
        assertEquals(2, jdbc.queryForObject("SELECT count(*) FROM af.payout_reference", Integer.class));
        command.setCommandType(CommandType.DELETE);
        dao.apply(command);
        assertEquals(1, jdbc.queryForObject("SELECT count(*) FROM af.payout_reference", Integer.class));
    }

    @Test
    void groupReplacementRollsBackIfAnyTemplateCannotBeSaved() {
        var group = new Group().setGroupId("synthetic-group").setOperationType(OperationType.payout)
                .setTemplateIds(List.of(new PriorityId(1, "old-template")));
        var command = create(CommandBody.group(group));
        dao.apply(command);
        group.setTemplateIds(List.of(new PriorityId(1, "duplicate"), new PriorityId(2, "duplicate")));
        assertThrows(RuntimeException.class, () -> dao.apply(command));
        assertEquals("old-template", jdbc.queryForObject(
                "SELECT template_id FROM af.payout_group WHERE group_id = 'synthetic-group'", String.class));
    }

    private Command create(CommandBody body) {
        return new Command().setCommandType(CommandType.CREATE).setCommandBody(body);
    }
}
