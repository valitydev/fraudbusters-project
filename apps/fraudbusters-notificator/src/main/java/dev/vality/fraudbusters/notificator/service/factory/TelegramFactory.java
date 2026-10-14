package dev.vality.fraudbusters.notificator.service.factory;

import dev.vality.fraudbusters.notificator.dao.ChannelDao;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Channel;
import dev.vality.fraudbusters.notificator.domain.Message;
import dev.vality.fraudbusters.notificator.domain.ReportModel;
import dev.vality.fraudbusters.notificator.utils.AttachmentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static dev.vality.fraudbusters.notificator.utils.ChannelUtils.initRecipient;

@Slf4j
@Service
@RequiredArgsConstructor
public class TelegramFactory {

    private final ChannelDao channelDao;
    private final AttachmentsService attachmentUtils;

    public Optional<Message> create(ReportModel reportModel) {
        String alertChannel = reportModel.getNotification().getChannel();
        Channel channel = channelDao.getByName(alertChannel);
        if (channel == null) {
            log.warn("Not found channel with name: {}", alertChannel);
            return Optional.empty();
        }
        String subject = reportModel.getNotification().getSubject();
        String content = reportModel.getNotificationTemplate().getSkeleton();
        return Optional.of(Message.builder()
                .content(content)
                .to(initRecipient(channel))
                .subject(subject)
                .attachment(attachmentUtils.initAttachment(reportModel, subject))
                .build());
    }

}