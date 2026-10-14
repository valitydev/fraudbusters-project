package dev.vality.fraudbusters.notificator.service.filter;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Notification;
import dev.vality.fraudbusters.notificator.dao.domain.tables.pojos.Report;
import dev.vality.fraudbusters.notificator.domain.ReportModel;
import dev.vality.fraudbusters.notificator.serializer.QueryResultSerde;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChangeQueryResultFilterTest {

    ChangeQueryResultFilter changeQueryResultFilter =
            new ChangeQueryResultFilter(new QueryResultSerde(JsonMapper.builder().build()));

    @Test
    void testFilterFalse() {
        Report lastReport = new Report();
        Report currentReport = new Report();

        lastReport.setResult("{\"results\":[{\"t\":\"2019-12-05\",\"metric\":\"166.66666666666666\"," +
                             "\"currency\":\"RUB\",\"shopId\":\"ad8b7bfd-0760-4781-a400-51903ee8e504\"}]}");
        currentReport.setResult("{\"results\":[{\"t\":\"2019-12-05\",\"metric\":\"166.66666666666666\"," +
                                "\"currency\":\"RUB\",\"shopId\":\"ad8b7bfd-0760-4781-a400-51903ee8e504\"}]}");

        boolean test = changeQueryResultFilter.test(ReportModel.builder()
                .previousReport(lastReport)
                .currentReport(currentReport)
                .notification(new Notification())
                .notificationTemplate(TestObjectsFactory.testNotificationTemplate("shopId,currency"))
                .build());

        assertFalse(test);
    }

    @Test
    void testFilterTrue() {
        Report lastReport = new Report();
        Report currentReport = new Report();

        lastReport.setResult("{\"results\":[{\"t\":\"2019-12-05\",\"metric\":\"166.66666666666666\"," +
                             "\"currency\":\"RUB\",\"shopId\":\"ad8b7bfd-0760-4781-a400-51903ee8e504\"}]}");
        currentReport.setResult("{\"results\":[{\"t\":\"2019-12-05\",\"metric\":\"166.66666666666666\"," +
                                "\"currency\":\"RUB\",\"shopId\":\"ad8b7bfd-0760-4781-a400-51903ee8e504\"}," +
                                "{\"t\":\"2019-12-05\",\"metric\":\"100\",\"currency\":\"USD\"," +
                                "\"shopId\":\"ad8b7bfd-0760-4781-a400-51903ee8e504\"}]}");

        boolean test = changeQueryResultFilter.test(ReportModel.builder()
                .previousReport(lastReport)
                .currentReport(currentReport)
                .notification(new Notification())
                .notificationTemplate(TestObjectsFactory.testNotificationTemplate("shopId,currency"))
                .build());

        assertTrue(test);
    }
}
