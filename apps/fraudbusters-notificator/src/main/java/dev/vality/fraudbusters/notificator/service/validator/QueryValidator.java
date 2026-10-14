package dev.vality.fraudbusters.notificator.service.validator;

import dev.vality.damsel.fraudbusters_notificator.Notification;
import dev.vality.fraudbusters.notificator.dao.NotificationTemplateDao;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.NotificationTemplate;
import dev.vality.fraudbusters.notificator.service.QueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueryValidator implements Validator {

    private final QueryService queryService;
    private final NotificationTemplateDao notificationTemplateDao;

    @Override
    public List<String> validate(Notification notification) {
        List<String> validationErrors = new ArrayList<>();
        NotificationTemplate notificationTemplate = notificationTemplateDao.getById((int) notification.getTemplateId());
        try {
            queryService.query(notificationTemplate.getQueryText());
        } catch (Exception e) {
            log.warn("Error when validate query to DB: {} e:", notificationTemplate.getQueryText(), e);
            validationErrors.add("Query has error! e: " + e.getCause());
        }
        return validationErrors;
    }

}
