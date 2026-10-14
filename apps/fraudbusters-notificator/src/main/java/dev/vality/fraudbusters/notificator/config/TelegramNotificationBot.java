package dev.vality.fraudbusters.notificator.config;

import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@RequiredArgsConstructor
public class TelegramNotificationBot extends TelegramLongPollingBot {

    @Value("${telegram.bot.username}")
    private String botUsername;

    private final VaultSecretService vaultSecretService;

    @Override
    public void onUpdateReceived(Update update) {

    }

    @Override
    public String getBotUsername() {
        return this.botUsername;
    }

    @Override
    public String getBotToken() {
        return vaultSecretService.getBotToken();
    }
}
