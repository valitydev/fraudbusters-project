package dev.vality.fraudbusters.notificator;

import dev.vality.fraudbusters.notificator.dao.NotificationDao;
import dev.vality.fraudbusters.notificator.dao.NotificationTemplateDao;
import dev.vality.fraudbusters.notificator.dao.ReportNotificationDao;
import dev.vality.fraudbusters.notificator.domain.ReportModel;
import dev.vality.fraudbusters.notificator.serializer.QueryResultSerde;
import dev.vality.fraudbusters.notificator.service.QueryService;
import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import dev.vality.fraudbusters.notificator.service.iface.NotificationService;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;
import java.util.function.Predicate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@PostgresqlTestcontainer
@SpringBootTest
public class ScheduledIntegrationTest {

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine");

    @MockitoBean
    NotificationDao notificationDao;
    @MockitoBean
    ReportNotificationDao reportNotificationDao;
    @MockitoBean
    NotificationTemplateDao notificationTemplateDao;
    @MockitoBean
    QueryService queryService;
    @MockitoBean
    QueryResultSerde queryResultSerde;
    @MockitoBean
    NotificationService notificationService;

    @MockitoBean
    @Qualifier("readyForNotifyFilter")
    Predicate<ReportModel> readyForNotifyFilter;

    @MockitoBean
    VaultSecretService vaultSecretService;

    @MockitoBean
    TelegramBotsApi telegramBotsApi;

    @BeforeEach
    void setUp() {
        Mockito.when(vaultSecretService.getBotToken()).thenReturn("test");
    }

    @Test
    void scheduleTest() throws InterruptedException {
        Thread.sleep(1000L);

        when(notificationDao.getByStatus(any())).thenReturn(List.of());
        verify(notificationDao, times(1)).getByStatus(any());
    }
}
