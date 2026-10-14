package dev.vality.fraudbusters.notificator.service.iface;

import dev.vality.fraudbusters.notificator.domain.Message;

public interface TelegramSenderService {

    boolean send(Message message);

}