package dev.vality.fraudbusters.notificator.processor;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ChannelRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.NotificationTemplateRecord;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ReportRecord;
import dev.vality.fraudbusters.notificator.domain.QueryResult;
import dev.vality.fraudbusters.notificator.service.QueryService;
import dev.vality.fraudbusters.notificator.service.VaultSecretService;
import dev.vality.fraudbusters.notificator.service.iface.MailSenderService;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.jooq.DSLContext;
import org.jooq.impl.DSL;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.util.List;
import java.util.Map;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@PostgresqlTestcontainer
@SpringBootTest
public class NotificationProcessorImplTest {

    @Autowired
    NotificationProcessorImpl notificationProcessor;

    @Autowired
    private DSLContext dslContext;

    JsonMapper objectMapper = JsonMapper.builder().build();

    @MockitoBean
    MailSenderService mailSenderService;

    @MockitoBean
    TelegramBotsApi telegramBotsApi;

    @MockitoBean
    QueryService queryService;

    @MockitoBean
    private VaultSecretService vaultSecretService;

    @BeforeEach
    void setUp() {
        dslContext.deleteFrom(NOTIFICATION).execute();
        Mockito.when(vaultSecretService.getBotToken()).thenReturn("test");
    }

    @Test
    void process() throws Exception {
        ChannelRecord channel = TestObjectsFactory.testChannelRecord();
        dslContext.insertInto(CHANNEL).set(channel).execute();
        dslContext.insertInto(NOTIFICATION_TEMPLATE).set(TestObjectsFactory.testNotificationTemplateRecord()).execute();
        NotificationTemplateRecord savedNotificationTemplate = dslContext.fetchAny(NOTIFICATION_TEMPLATE);
        NotificationRecord successNotification = TestObjectsFactory.testNotificationRecord();
        successNotification.setChannel(channel.getName());
        successNotification.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION).set(successNotification).execute();
        NotificationRecord errorNotification = TestObjectsFactory.testNotificationRecord();
        errorNotification.setTemplateId(savedNotificationTemplate.getId());
        dslContext.insertInto(NOTIFICATION).set(errorNotification).execute();
        String shopId = TestObjectsFactory.randomString();
        when(queryService.query(anyString())).thenReturn(List.of(Map.of("shopId", shopId)));

        notificationProcessor.process();

        List<ReportRecord> notificationByStatus = dslContext.selectFrom(REPORT).where(REPORT.ID.in(
                dslContext.select(DSL.max(REPORT.ID)).from(REPORT).where(REPORT.STATUS.eq(ReportStatus.send)))).fetch();

        String result = notificationByStatus.get(0).getResult();
        QueryResult queryResult = objectMapper.readValue(result, QueryResult.class);
        assertEquals(shopId, queryResult.getResults().get(0).get("shopId"));

        notificationProcessor.process();

        notificationByStatus = dslContext.selectFrom(REPORT).where(REPORT.ID.in(
                        dslContext.select(DSL.max(REPORT.ID)).from(REPORT)
                                .where(REPORT.STATUS.eq(ReportStatus.created))))
                .fetch();
        assertEquals(0L, notificationByStatus.size());

        Thread.sleep(1000L);

        notificationProcessor.process();

        notificationByStatus = dslContext.selectFrom(REPORT).where(REPORT.ID.in(
                        dslContext.select(DSL.max(REPORT.ID)).from(REPORT)
                                .where(REPORT.STATUS.eq(ReportStatus.skipped))))
                .fetch();
        assertEquals(0L, notificationByStatus.size());
        verify(mailSenderService, times(3)).send(any());
    }
}