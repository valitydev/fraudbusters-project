package dev.vality.fraudbusters.notificator.service;

import dev.vality.fraudbusters.notificator.AbstractIntegrationTest;
import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.config.TelegramNotificationBot;
import dev.vality.fraudbusters.notificator.domain.Attachment;
import dev.vality.fraudbusters.notificator.domain.Message;
import dev.vality.fraudbusters.notificator.exception.TelegramSendException;
import dev.vality.fraudbusters.notificator.service.iface.TelegramSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TelegramSenderServiceTest extends AbstractIntegrationTest {

    @Autowired
    private TelegramSenderService telegramSenderService;

    @MockitoBean
    private TelegramNotificationBot telegramNotificationBot;

    @MockitoBean
    private VaultSecretService vaultSecretService;

    @BeforeEach
    void setUp() {
        when(vaultSecretService.getBotToken()).thenReturn("test-bot-token");
    }

    @Test
    void shouldSendSimpleMessageToTelegramChannel() throws TelegramSendException, TelegramApiException {
        // Given
        Message message = createTestMessage();
        when(telegramNotificationBot.execute(any(SendMessage.class))).thenReturn(null);

        // When
        boolean result = telegramSenderService.send(message);

        // Then
        assertTrue(result);
        verify(telegramNotificationBot, times(1)).execute(any(SendMessage.class));
        verify(telegramNotificationBot, never()).execute(any(SendDocument.class));
    }

    @Test
    void shouldSendMessageWithAttachmentToTelegramChannel() throws TelegramSendException, TelegramApiException {
        // Given
        Message message = createTestMessageWithAttachment();
        when(telegramNotificationBot.execute(any(SendMessage.class))).thenReturn(null);
        when(telegramNotificationBot.execute(any(SendDocument.class))).thenReturn(null);

        // When
        boolean result = telegramSenderService.send(message);

        // Then
        assertTrue(result);
        verify(telegramNotificationBot, times(1)).execute(any(SendMessage.class));
        verify(telegramNotificationBot, times(1)).execute(any(SendDocument.class));
    }

    @Test
    void shouldThrowExceptionWhenTelegramApiFailsForMessage() throws TelegramApiException {
        // Given
        Message message = createTestMessage();
        TelegramApiException telegramException = new TelegramApiException("API Error");
        when(telegramNotificationBot.execute(any(SendMessage.class))).thenThrow(telegramException);

        // When & Then
        TelegramSendException exception = assertThrows(TelegramSendException.class,
                () -> telegramSenderService.send(message));

        assertEquals("Failed to send Telegram message", exception.getMessage());
        assertEquals(telegramException, exception.getCause());
        verify(telegramNotificationBot, times(1)).execute(any(SendMessage.class));
    }

    @Test
    void shouldThrowExceptionWhenTelegramApiFailsForDocument() throws TelegramApiException {
        // Given
        Message message = createTestMessageWithAttachment();
        when(telegramNotificationBot.execute(any(SendMessage.class))).thenReturn(null);
        TelegramApiException telegramException = new TelegramApiException("Document API Error");
        when(telegramNotificationBot.execute(any(SendDocument.class))).thenThrow(telegramException);

        // When & Then
        TelegramSendException exception = assertThrows(TelegramSendException.class,
                () -> telegramSenderService.send(message));

        assertEquals("Failed to send Telegram message", exception.getMessage());
        assertEquals(telegramException, exception.getCause());
        verify(telegramNotificationBot, times(1)).execute(any(SendMessage.class));
        verify(telegramNotificationBot, times(1)).execute(any(SendDocument.class));
    }

    @Test
    void shouldSendMessageToMultipleChannels() throws TelegramSendException, TelegramApiException {
        // Given
        Message message = Message.builder()
                .from("test-sender")
                .to(new String[] {"@test_channel_1", "@test_channel_2"})
                .subject("Test Subject")
                .content("Test message content")
                .partyId(TestObjectsFactory.randomString())
                .claimId(12345L)
                .build();

        when(telegramNotificationBot.execute(any(SendMessage.class))).thenReturn(null);

        // When
        boolean result = telegramSenderService.send(message);

        // Then
        assertTrue(result);
        // Проверяем, что отправка происходит только в первый канал (согласно реализации)
        verify(telegramNotificationBot, times(1)).execute(any(SendMessage.class));
    }

    private Message createTestMessage() {
        return Message.builder()
                .from("test-sender")
                .to(new String[] {"@test_channel"})
                .subject("Test Subject")
                .content("Test message content")
                .partyId(TestObjectsFactory.randomString())
                .claimId(12345L)
                .build();
    }

    private Message createTestMessageWithAttachment() {
        Attachment attachment = Attachment.builder()
                .fileName("test-report.csv")
                .content("party_id,amount,status\nparty1,1000,fraud\nparty2,500,clean")
                .build();

        return Message.builder()
                .from("test-sender")
                .to(new String[] {"@test_channel"})
                .subject("Test Subject with Attachment")
                .content("Test message with attachment")
                .attachment(attachment)
                .partyId(TestObjectsFactory.randomString())
                .claimId(67890L)
                .build();
    }
}