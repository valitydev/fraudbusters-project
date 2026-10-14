package dev.vality.fraudbusters.notificator.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Slf4j
@Configuration
public class TelegramConfig {

    @Bean
    public TelegramBotsApi telegramBotsApi(TelegramNotificationBot telegramBot) {
        try {
            var api = new TelegramBotsApi(DefaultBotSession.class);
            api.registerBot(telegramBot);
            log.info("Telegram bot successfully started");
            return api;
        } catch (TelegramApiException e) {
            log.error("Error when register Telegram bot", e);
            return null;
        }
    }

    @Bean
    public DefaultBotOptions defaultBotOptions() {
        return new DefaultBotOptions();
    }

}