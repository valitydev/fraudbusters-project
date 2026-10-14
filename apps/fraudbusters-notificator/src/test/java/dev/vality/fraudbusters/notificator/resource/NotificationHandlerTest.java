package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.*;
import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationTemplateRecord;
import dev.vality.fraudbusters.notificator.exception.WarehouseQueryException;
import dev.vality.fraudbusters.notificator.service.QueryService;
import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.apache.thrift.TException;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dev.vality.damsel.fraudbusters_notificator.fraudbusters_notificatorConstants.VALIDATION_ERROR;
import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION;
import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION_TEMPLATE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@PostgresqlTestcontainer
@SpringBootTest
class NotificationHandlerTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private NotificationHandler notificationHandler;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    QueryService queryService;

    @MockitoBean
    VaultSecretService vaultSecretService;

    @BeforeEach
    void setUp() {
        Mockito.when(vaultSecretService.getBotToken()).thenReturn("test");
        dslContext.deleteFrom(NOTIFICATION).execute();
    }

    @Test
    void createOrUpdateWithValidationError() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        Notification notification = TestObjectsFactory.testNotification();
        notification.setTemplateId(savedNotificationTemplate.getId());
        when(queryService.query(savedNotificationTemplate.getQueryText()))
                .thenThrow(new WarehouseQueryException(new TException()));

        NotificationServiceException exception =
                assertThrows(NotificationServiceException.class, () -> notificationHandler.create(notification));

        assertEquals(VALIDATION_ERROR, exception.getCode());
        assertThat(exception.getReason(), containsString("Query has error"));

    }

    @Test
    void createOrUpdate() throws TException {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        Notification notification = TestObjectsFactory.testNotification();
        notification.setTemplateId(savedNotificationTemplate.getId());
        Map<String, String> values = new HashMap<>();
        values.put("key", "value");
        List<Map<String, String>> queryResult = List.of(values);
        when(queryService.query(savedNotificationTemplate.getQueryText())).thenReturn(queryResult);

        Notification createdNotification = notificationHandler.create(notification);

        assertFalse(dslContext.fetch(NOTIFICATION).isEmpty());
        assertTrue(createdNotification.getId() != 0);
        assertEquals(notification.getName(), createdNotification.getName());
        assertEquals(notification.getFrequency(), createdNotification.getFrequency());
        assertEquals(notification.getPeriod(), createdNotification.getPeriod());
        assertEquals(notification.getTemplateId(), createdNotification.getTemplateId());
        assertEquals(notification.getChannel(), createdNotification.getChannel());
    }

    @Test
    void delete() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord notification = TestObjectsFactory.testNotificationRecord();
        notification.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification)
                .execute();
        NotificationRecord savedNotification = dslContext.fetchOne(NOTIFICATION);

        notificationHandler.remove(savedNotification.getId());

        assertTrue(dslContext.fetch(NOTIFICATION).isEmpty());
    }

    @Test
    void setStatus() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord notification = TestObjectsFactory.testNotificationRecord();
        notification.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification)
                .execute();
        NotificationRecord savedNotification = dslContext.fetchOne(NOTIFICATION);
        NotificationStatus newStatus = NotificationStatus.CREATED;

        notificationHandler.updateStatus(savedNotification.getId(), newStatus);

        NotificationRecord updatedNotification = dslContext.fetchOne(NOTIFICATION);
        assertEquals(newStatus.name(), updatedNotification.getStatus().getLiteral());
    }

    @Test
    void validateWithFieldError() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        Notification notification = TestObjectsFactory.testNotification();
        notification.setTemplateId(savedNotificationTemplate.getId());
        notification.setName(null);
        notification.setChannel(null);

        ValidationResponse validationResponse = notificationHandler.validate(notification);

        assertEquals(2, validationResponse.getErrors().size());
    }

    @Test
    void validateWithQueryError() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        Notification notification = TestObjectsFactory.testNotification();
        notification.setTemplateId(savedNotificationTemplate.getId());
        when(queryService.query(savedNotificationTemplate.getQueryText()))
                .thenThrow(new WarehouseQueryException(new TException()));

        ValidationResponse validationResponse = notificationHandler.validate(notification);

        assertEquals(1, validationResponse.getErrors().size());
        assertThat(validationResponse.getErrors().get(0), containsString("Query has error"));
    }

    @Test
    void validateWithAllErrors() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        Notification notification = TestObjectsFactory.testNotification();
        notification.setTemplateId(savedNotificationTemplate.getId());
        notification.setName(null);
        notification.setChannel(null);
        notification.setFrequency(null);
        when(queryService.query(anyString())).thenThrow(new WarehouseQueryException(new TException()));

        ValidationResponse validationResponse = notificationHandler.validate(notification);

        assertEquals(4, validationResponse.getErrors().size());
    }

    @Test
    void validateOk() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        Notification notification = TestObjectsFactory.testNotification();
        notification.setTemplateId(savedNotificationTemplate.getId());
        Map<String, String> values = new HashMap<>();
        values.put("key", "value");
        List<Map<String, String>> queryResult = List.of(values);
        when(queryService.query(savedNotificationTemplate.getQueryText())).thenReturn(queryResult);

        ValidationResponse validationResponse = notificationHandler.validate(notification);

        assertEquals(String.valueOf(queryResult), validationResponse.getResult());
    }

    @Test
    void getAll() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord notification1 = TestObjectsFactory.testNotificationRecord();
        notification1.setTemplateId(savedNotificationTemplate.getId());
        NotificationRecord notification2 = TestObjectsFactory.testNotificationRecord();
        notification2.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification1)
                .newRecord()
                .set(notification2)
                .execute();

        NotificationListResponse result = notificationHandler.getAll(new Page(), new Filter());

        assertEquals(2, result.getNotificationsSize());
        assertTrue(result.getNotifications().stream().map(Notification::getName)
                .anyMatch(s -> s.equals(notification1.getName())));
        assertTrue(result.getNotifications().stream().map(Notification::getName)
                .anyMatch(s -> s.equals(notification2.getName())));
    }

    @Test
    void getAllWithFilter() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord notification1 = TestObjectsFactory.testNotificationRecord();
        notification1.setTemplateId(savedNotificationTemplate.getId());
        NotificationRecord notification2 = TestObjectsFactory.testNotificationRecord();
        notification2.setTemplateId(savedNotificationTemplate.getId());
        NotificationRecord notification3 = TestObjectsFactory.testNotificationRecord();
        notification3.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification1)
                .newRecord()
                .set(notification2)
                .newRecord()
                .set(notification3)
                .execute();
        Page page = new Page();
        page.setSize(2);

        NotificationListResponse result = notificationHandler.getAll(page, new Filter());

        assertEquals(2, result.getNotificationsSize());
        assertTrue(result.getNotifications().stream().map(Notification::getName)
                .anyMatch(s -> s.equals(notification1.getName())));
        assertTrue(result.getNotifications().stream().map(Notification::getName)
                .anyMatch(s -> s.equals(notification2.getName())));
        var continuationNotification =
                dslContext.selectFrom(NOTIFICATION).where(NOTIFICATION.NAME.eq(notification2.getName())).fetchOne();
        assertEquals(continuationNotification.getId(), result.getContinuationId());
    }

    @Test
    void getById() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord notification = TestObjectsFactory.testNotificationRecord();
        notification.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification)
                .execute();

        NotificationRecord savedNotification = dslContext.fetchOne(NOTIFICATION);

        Notification result = notificationHandler.getById(savedNotification.getId());

        assertEquals(notification.getName(), result.getName());
        assertEquals(notification.getSubject(), result.getSubject());
        assertEquals(savedNotification.getId(), result.getId());
        assertEquals(notification.getChannel(), result.getChannel());
    }
}
