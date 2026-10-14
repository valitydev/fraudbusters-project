package dev.vality.fraudbusters.notificator.utils;

import dev.vality.fraudbusters.notificator.domain.Attachment;
import dev.vality.fraudbusters.notificator.domain.ReportModel;
import dev.vality.fraudbusters.notificator.serializer.QueryResultSerde;
import dev.vality.fraudbusters.notificator.service.factory.AttachmentFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AttachmentsService {

    private final QueryResultSerde queryResultSerde;
    private final AttachmentFactory attachmentFactory;

    public Attachment initAttachment(ReportModel reportModel, String subject) {
        return queryResultSerde.deserialize(reportModel.getCurrentReport().getResult())
                .map(queryResult ->
                        Attachment.builder()
                                .content(attachmentFactory.create(queryResult.getResults()))
                                .fileName(attachmentFactory.createNameOfAttachment(subject))
                                .build())
                .orElse(null);
    }

}
