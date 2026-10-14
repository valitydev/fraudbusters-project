package dev.vality.fraudbusters.notificator.resource;

import dev.vality.fraudbusters.notificator.utils.TestObjectsFactory;
import dev.vality.fraudbusters.notificator.dao.domain.enums.ReportStatus;
import dev.vality.fraudbusters.notificator.dao.domain.tables.records.ReportRecord;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import org.jooq.DSLContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;

import java.time.LocalDateTime;

import static dev.vality.fraudbusters.notificator.dao.domain.Tables.REPORT;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@PostgresqlTestcontainer
@SpringBootTest
class ReportResourceImplTest {

    @Autowired
    private WebApplicationContext context;

    protected MockMvc mockMvc;

    @Autowired
    private DSLContext dslContext;

    @MockitoBean
    TelegramBotsApi telegramBotsApi;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.context).build();
    }

    @Test
    void findReportsByStatusAndFromTimeTest() throws Exception {
        ReportRecord report1 = TestObjectsFactory.testReportRecord();
        report1.setCreatedAt(LocalDateTime.now().minusDays(1));
        ReportRecord report2 = TestObjectsFactory.testReportRecord();
        report2.setCreatedAt(LocalDateTime.now().plusDays(1));
        ReportRecord report3 = TestObjectsFactory.testReportRecord();
        report3.setStatus(ReportStatus.failed);
        dslContext.insertInto(REPORT)
                .set(report1)
                .newRecord()
                .set(report2)
                .newRecord()
                .set(report3)
                .execute();

        mockMvc.perform(MockMvcRequestBuilders.get("/reports")
                        .param("status", "send")
                        .param("from", LocalDateTime.now().toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].result", is(report1.getResult())))
                .andExpect(status().isOk());
    }
}