package dev.vality.fraudbusters.notificator.resource.converter;

import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

@Component
public class NotificationTemplateConverter
        implements
        Converter<NotificationTemplate, dev.vality.damsel.fraudbusters_notificator.NotificationTemplate> {

    @Override
    public dev.vality.damsel.fraudbusters_notificator.NotificationTemplate convert(
            NotificationTemplate notificationTemplate) {
        var result = new dev.vality.damsel.fraudbusters_notificator.NotificationTemplate();
        result.setId(notificationTemplate.getId());
        result.setName(notificationTemplate.getName());
        result.setBasicParams(notificationTemplate.getBasicParams());
        result.setQueryText(notificationTemplate.getQueryText());
        result.setSkeleton(notificationTemplate.getSkeleton());
        result.setType(notificationTemplate.getType());
        if (Objects.nonNull(notificationTemplate.getUpdatedAt())) {
            result.setUpdatedAt(notificationTemplate.getUpdatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        if (Objects.nonNull(notificationTemplate.getCreatedAt())) {
            result.setCreatedAt(notificationTemplate.getCreatedAt().format(DateTimeFormatter.ISO_DATE_TIME));
        }
        return result;
    }
}
