package dev.vality.fraudbusters.management.resource.notificator.converter;

import dev.vality.swag.fraudbusters.management.model.NotificationTemplate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class NotificationTemplateConverter
        implements
        Converter<dev.vality.damsel.fraudbusters_notificator.NotificationTemplate, NotificationTemplate> {

    @Override
    public NotificationTemplate convert(
            dev.vality.damsel.fraudbusters_notificator.NotificationTemplate notificationTemplate) {
        NotificationTemplate result = new NotificationTemplate();
        result.setId(notificationTemplate.getId());
        result.setName(notificationTemplate.getName());
        result.setBasicParams(notificationTemplate.getBasicParams());
        result.setQueryText(notificationTemplate.getQueryText());
        result.setSkeleton(notificationTemplate.getSkeleton());
        result.setType(notificationTemplate.getType());
        if (notificationTemplate.isSetUpdatedAt()) {
            result.setUpdatedAt(
                    LocalDateTime.parse(notificationTemplate.getUpdatedAt(), DateTimeFormatter.ISO_DATE_TIME));
        }
        if (notificationTemplate.isSetCreatedAt()) {
            result.setCreatedAt(
                    LocalDateTime.parse(notificationTemplate.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME));
        }
        return result;
    }
}
