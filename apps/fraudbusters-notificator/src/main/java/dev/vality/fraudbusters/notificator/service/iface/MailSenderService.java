package dev.vality.fraudbusters.notificator.service.iface;

import dev.vality.fraudbusters.notificator.domain.Message;

public interface MailSenderService {

    boolean send(Message message);

}
