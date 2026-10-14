package dev.vality.fraudbusters.notificator.service.validator;

import dev.vality.damsel.fraudbusters_notificator.Notification;
import dev.vality.fraudbusters.notificator.parser.PeriodParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class FieldValidator implements Validator {

    private final PeriodParser periodParser;

    @Override
    public List<String> validate(Notification notification) {
        List<String> validationErrors = new ArrayList<>();
        if (!StringUtils.hasLength(notification.getPeriod())) {
            log.warn("Empty required field period");
            validationErrors.add("Empty required field period");
        } else if (periodParser.parse(notification.getPeriod()) == 0L) {
            log.warn("Unknown period value");
            validationErrors.add("Unknown period value: " + notification.getPeriod());
        }

        validateField(validationErrors, notification.getName(), "Empty name", "Name is required");
        validateField(validationErrors, notification.getChannel(), "Empty channel", "Channel is required");
        validateField(validationErrors, notification.getFrequency(), "Empty frequency", "frequency  is required");

        return validationErrors;
    }

    private void validateField(List<String> validationErrors,
                               String field,
                               String logMessage,
                               String errorMessage) {
        if (!StringUtils.hasLength(field)) {
            log.warn(logMessage);
            validationErrors.add(errorMessage);
        }
    }
}
