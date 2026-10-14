package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.NotificationTemplateListResponse;
import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
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

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION;
import static dev.vality.fraudbusters.notificator.dao.domain.Tables.NOTIFICATION_TEMPLATE;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles("test")
@PostgresqlTestcontainer
@SpringBootTest
class NotificationTemplateHandlerTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine");

    @Autowired
    private NotificationTemplateHandler notificationTemplateHandler;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    VaultSecretService vaultSecretService;

    @BeforeEach
    void setUp() {
        Mockito.when(vaultSecretService.getBotToken()).thenReturn("test");
        dslContext.deleteFrom(NOTIFICATION).execute();
        dslContext.deleteFrom(NOTIFICATION_TEMPLATE).execute();
    }

    @Test
    void getAll() {
        dslContext.insertInto(NOTIFICATION_TEMPLATE)
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .newRecord()
                .set(TestObjectsFactory.testNotificationTemplateRecord())
                .execute();
        NotificationTemplateListResponse result = notificationTemplateHandler.getAll();

        assertEquals(2, result.getNotificationTemplatesSize());
    }
}