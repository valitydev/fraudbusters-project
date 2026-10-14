package dev.vality.fraudbusters.notificator.resource;

import dev.vality.damsel.fraudbusters_notificator.*;
import dev.vality.fraudbusters.notificator.dao.NotificationDao;
import dev.vality.fraudbusters.notificator.dao.NotificationTemplateDao;
import dev.vality.fraudbusters.notificator.resource.converter.NotificationConverter;
import dev.vality.fraudbusters.notificator.service.QueryService;
import dev.vality.fraudbusters.notificator.service.dto.FilterDto;
import dev.vality.fraudbusters.notificator.service.validator.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.vality.damsel.fraudbusters_notificator.fraudbusters_notificatorConstants.VALIDATION_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHandler implements NotificationServiceSrv.Iface {

    private final NotificationDao notificationDao;
    private final NotificationTemplateDao notificationTemplateDao;
    private final QueryService queryService;
    private final List<Validator> validators;
    private final NotificationConverter notificationConverter;
    private final FilterConverter filterConverter;

    @Override
    public Notification create(Notification notification) throws TException {
        var validationResult = validate(notification);
        if (validationResult.isSetErrors()) {
            throw new NotificationServiceException()
                    .setCode(VALIDATION_ERROR)
                    .setReason("Error while validate notification, errors: " +
                               String.join(", ", validationResult.getErrors()));
        }
        var savedNotification = notificationDao.insert(notificationConverter.toTarget(notification));
        log.info("NotificationHandler create notification: {}", savedNotification);
        return notificationConverter.toSource(savedNotification);
    }

    @Override
    public void remove(long id) {
        notificationDao.remove(id);
        log.info("NotificationHandler delete notification with id: {}", id);
    }

    @Override
    public void updateStatus(long id, NotificationStatus status) {
        var notification = notificationDao.getById(id);
        notification.setStatus(
                dev.vality.fraudbusters.notificator.dao.domain.enums.NotificationStatus.valueOf(status.name()));
        notificationDao.insert(notification);
        log.info("NotificationHandler change status notification: {}", notification);
    }

    @Override
    public ValidationResponse validate(Notification notification) {
        List<String> errors = validators.stream()
                .map(validator -> validator.validate(notification))
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
        var validationResponse = new ValidationResponse();
        if (!CollectionUtils.isEmpty(errors)) {
            validationResponse.setErrors(errors);
            log.info("NotificationHandler notification validation failed with errors: {}", errors);
            return validationResponse;
        }
        var notificationTemplate = notificationTemplateDao.getById((int) notification.getTemplateId());
        List<Map<String, String>> result = queryService.query(notificationTemplate.getQueryText());
        validationResponse.setResult(String.valueOf(result));
        return validationResponse;
    }

    @Override
    public NotificationListResponse getAll(Page page, Filter filter) {
        FilterDto filterDto = filterConverter.convert(page, filter);
        var notifications = notificationDao.getAll(filterDto);
        log.info("NotificationHandler get all notifications: {}", notifications);
        List<Notification> result = notifications.stream()
                .map(notificationConverter::toSource)
                .collect(Collectors.toList());
        NotificationListResponse notificationListResponse = new NotificationListResponse()
                .setNotifications(result);
        if (notifications.size() == filterDto.getSize()) {
            var lastNotification = notifications.getLast();
            notificationListResponse.setContinuationId(lastNotification.getId());
        }
        return notificationListResponse;
    }

    @Override
    public Notification getById(long id) {
        var notification = notificationDao.getById(id);
        log.info("NotificationHandler get notification by Id {} notification: {}", id, notification);
        return notificationConverter.toSource(notification);
    }
}
