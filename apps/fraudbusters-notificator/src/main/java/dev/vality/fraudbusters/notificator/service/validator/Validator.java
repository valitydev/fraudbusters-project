package dev.vality.fraudbusters.notificator.service.validator;

import dev.vality.damsel.fraudbusters_notificator.Notification;

import java.util.List;

public interface Validator {

    List<String> validate(Notification notification);

}
