package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.notificator.AbstractIntegrationTest;
import dev.vality.fraudbusters.notificator.config.TelegramNotificationBot;
import dev.vality.fraudbusters.notificator.dao.ChannelDao;
import dev.vality.fraudbusters.notificator.dao.ReportNotificationDao;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import dev.vality.fraudbusters.notificator.domain.ReportModel;
import dev.vality.fraudbusters.notificator.service.iface.NotificationService;
import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.warehouse.Row;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TelegramNotificationIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private NotificationService notificationService;

    @MockitoBean
    private TelegramNotificationBot telegramNotificationBot;

    @MockitoBean
    private VaultSecretService vaultSecretService;

    @MockitoBean
    private ReportNotificationDao reportNotificationDao;

    @MockitoBean
    private ChannelDao channelDao;

    @BeforeEach
    void setUp() {
        when(vaultSecretService.getBotToken()).thenReturn("test-bot-token");
        when(channelDao.getByName(any())).thenReturn(TestObjectsFactory.createChannel());
    }

    @Test
    void shouldSendTelegramNotificationForFraudReport() throws TelegramApiException {
        // Given
        when(telegramNotificationBot.execute(any(SendMessage.class))).thenReturn(null);

        ReportModel reportModel = createTestReportModel();

        // When
        notificationService.send(reportModel);

        // Then
        verify(telegramNotificationBot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void shouldHandleTelegramApiExceptionGracefully() throws TelegramApiException {
        // Given
        when(telegramNotificationBot.execute(any(SendMessage.class)))
                .thenThrow(new TelegramApiException("Channel not found"));

        ReportModel reportModel = createTestReportModel();

        // When & Then - должно логировать ошибку, но не падать
        notificationService.send(reportModel);

        verify(telegramNotificationBot, times(1)).execute(any(SendMessage.class));
    }

    private ReportModel createTestReportModel() {
        Row row = TestObjectsFactory.testRow();
        row.getValues().put("partyId", "test-party-123");
        row.getValues().put("amount", "1500.00");

        Notification notification = TestObjectsFactory.createNotification();
        NotificationTemplate template = TestObjectsFactory.testNotificationTemplate("shopId,currency");
        Channel channel = TestObjectsFactory.createChannel();
        Report currentReport = TestObjectsFactory.createReport();
        return ReportModel.builder()
                .channel(channel)
                .notificationTemplate(template)
                .notification(notification)
                .currentReport(currentReport)
                .build();
    }

}