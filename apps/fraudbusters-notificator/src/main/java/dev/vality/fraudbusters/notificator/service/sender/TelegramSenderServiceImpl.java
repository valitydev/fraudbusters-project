package dev.vality.fraudbusters.notificator.service.sender;

import dev.vality.fraudbusters.notificator.config.TelegramNotificationBot;
import dev.vality.fraudbusters.notificator.domain.Message;
import dev.vality.fraudbusters.notificator.exception.TelegramSendException;
import dev.vality.fraudbusters.notificator.service.iface.TelegramSenderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.ByteArrayInputStream;

@Slf4j
@Component
@RequiredArgsConstructor
public class TelegramSenderServiceImpl implements TelegramSenderService {

    private final TelegramNotificationBot telegramNotificationBot;

    @Override
    public boolean send(Message message) throws TelegramSendException {
        try {
            log.info("Sending Telegram message for party: {}, claim: {}, to: {}",
                    message.getPartyId(), message.getClaimId(), message.getTo());

            SendMessage sendMessage = new SendMessage();
            sendMessage.setChatId(message.getTo()[0]);
            sendMessage.setText("Subject: " + message.getSubject() + "\n\n" + message.getContent());

            telegramNotificationBot.execute(sendMessage);
            if (message.getAttachment() != null) {
                SendDocument sendDocument = new SendDocument();
                sendDocument.setChatId(message.getTo()[0]);
                sendDocument.setDocument(
                        new InputFile(new ByteArrayInputStream(message.getAttachment().getContent().getBytes()),
                                message.getAttachment().getFileName()));
                telegramNotificationBot.execute(sendDocument);
            }
            return true;
        } catch (TelegramApiException e) {
            log.error("Failed to send Telegram message for party: {}, claim: {}, to: {}",
                    message.getPartyId(), message.getClaimId(), message.getTo(), e);
            throw new TelegramSendException("Failed to send Telegram message", e);
        }
    }
}