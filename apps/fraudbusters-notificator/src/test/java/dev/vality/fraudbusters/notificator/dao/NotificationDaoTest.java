package dev.vality.fraudbusters.notificator.dao;

import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationTemplateRecord;
import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION;
import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION_TEMPLATE;
import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@PostgresqlTestcontainer
@SpringBootTest
public class NotificationDaoTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    DSLContext dslContext;

    @Autowired
    NotificationDao notificationDao;

    @MockitoBean
    private VaultSecretService vaultSecretService;

    @BeforeEach
    void setUp() {
        Mockito.when(vaultSecretService.getBotToken()).thenReturn("test");
        dslContext.deleteFrom(NOTIFICATION).execute();
    }

    @Test
    void testCreate() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        Notification notification = TestObjectsFactory.testTableNotification();
        notification.setTemplateId(savedNotificationTemplate.getId());

        Notification savedNotification = notificationDao.insert(notification);

        assertNotNull(savedNotification.getId());
        assertEquals(notification.getPeriod(), savedNotification.getPeriod());
        assertEquals(notification.getName(), savedNotification.getName());

    }

    @Test
    void testGetById() {
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

        Notification actualNotification = notificationDao.getById(savedNotification.getId());

        assertEquals(notification.getPeriod(), actualNotification.getPeriod());
        assertEquals(notification.getName(), actualNotification.getName());
        assertEquals(notification.getChannel(), actualNotification.getChannel());
        assertEquals(notification.getSubject(), actualNotification.getSubject());
    }

    @Test
    void testGetByStatus() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord notification = TestObjectsFactory.testNotificationRecord();
        notification.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification)
                .execute();

        List<Notification> notifications = notificationDao.getByStatus(notification.getStatus());

        assertEquals(1, notifications.size());
        assertEquals(notification.getName(), notifications.get(0).getName());
    }

    @Test
    void testUpdate() {
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
        String newName = TestObjectsFactory.randomString();
        Notification notificationToUpdate = savedNotification.into(Notification.class);
        notificationToUpdate.setName(newName);
        notificationToUpdate.setUpdatedAt(LocalDateTime.now());

        Notification updatedNotification = notificationDao.insert(notificationToUpdate);

        assertEquals(newName, updatedNotification.getName());
        assertNotEquals(savedNotification.getUpdatedAt(), updatedNotification.getUpdatedAt());
    }

    @Test
    void testRemove() {
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

        notificationDao.remove(savedNotification.getId());

        Result<NotificationRecord> notifications = dslContext.fetch(NOTIFICATION);
        assertTrue(notifications.isEmpty());
    }

    @Test
    void testGetAll() {
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

        List<Notification> all = notificationDao.getAll(new FilterDto());

        assertEquals(2, all.size());
        assertIterableEquals(List.of(notification1.getName(), notification2.getName()),
                all.stream()
                        .map(Notification::getName)
                        .collect(Collectors.toList()));

    }

    @Test
    void testGetAllWithFilterContinuationId() {
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
        NotificationRecord notification4 = TestObjectsFactory.testNotificationRecord();
        notification4.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification1)
                .newRecord()
                .set(notification2)
                .newRecord()
                .set(notification3)
                .newRecord()
                .set(notification4)
                .execute();

        Result<NotificationRecord> savedNotifications = dslContext.fetch(NOTIFICATION);

        FilterDto filter = new FilterDto();
        filter.setContinuationId(savedNotifications.get(1).getId());
        List<Notification> all = notificationDao.getAll(filter);

        assertEquals(2, all.size());
        assertIterableEquals(List.of(notification3.getName(), notification4.getName()),
                all.stream()
                        .map(Notification::getName)
                        .collect(Collectors.toList()));

    }

    @Test
    void testGetAllWithFilterSearchField() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord notification1 = TestObjectsFactory.testNotificationRecord();
        notification1.setTemplateId(savedNotificationTemplate.getId());
        NotificationRecord notification2 = TestObjectsFactory.testNotificationRecord();
        notification2.setTemplateId(savedNotificationTemplate.getId());
        notification2.setName(notification1.getName() + notification2.getName());
        NotificationRecord notification3 = TestObjectsFactory.testNotificationRecord();
        notification3.setTemplateId(savedNotificationTemplate.getId());
        notification3.setName(notification3.getName() + notification1.getName());
        NotificationRecord notification4 = TestObjectsFactory.testNotificationRecord();
        notification4.setTemplateId(savedNotificationTemplate.getId());
        notification4.setName(notification4.getName() + notification1.getName() + notification4.getName());
        NotificationRecord notification5 = TestObjectsFactory.testNotificationRecord();
        notification5.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION)
                .set(notification1)
                .newRecord()
                .set(notification2)
                .newRecord()
                .set(notification3)
                .newRecord()
                .set(notification4)
                .newRecord()
                .set(notification5)
                .execute();

        FilterDto filter = new FilterDto();
        filter.setSearchFiled(notification1.getName());
        List<Notification> all = notificationDao.getAll(filter);

        assertEquals(4, all.size());
        assertIterableEquals(List.of(notification1.getSubject(), notification2.getSubject(), notification3.getSubject(),
                        notification4.getSubject()),
                all.stream()
                        .map(Notification::getSubject)
                        .collect(Collectors.toList()));

    }

    @Test
    void testGetAllWithFilterSize() {
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

        FilterDto filter = new FilterDto();
        filter.setSize(2L);
        List<Notification> all = notificationDao.getAll(filter);

        assertEquals(2, all.size());
        assertIterableEquals(List.of(notification1.getName(), notification2.getName()),
                all.stream()
                        .map(Notification::getName)
                        .collect(Collectors.toList()));

    }
}
