package dev.vality.fraudbusters.management.kafka;

import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.fraudbusters.management.config.KafkaITest;
import dev.vality.fraudbusters.management.controller.ErrorController;
import dev.vality.fraudbusters.management.dao.payment.wblist.WbListDao;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import dev.vality.fraudbusters.management.resource.payment.PaymentsListsResource;
import dev.vality.testcontainers.annotations.kafka.config.KafkaConsumer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@KafkaITest
public class DeleteFromListTest {

    @Value("${kafka.topic.wblist.command}")
    public String topicCommand;
    @MockitoBean
    public WbListDao wbListDao;

    @Autowired
    private KafkaConsumer<ChangeCommand> testCommandKafkaConsumer;

    @Autowired
    private PaymentsListsResource paymentsListsResource;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(paymentsListsResource, new ErrorController()).build();
    }

    @Test
    void deleteFromList() throws Exception {
        String test = "test";
        when(wbListDao.getById(test)).thenReturn(new WbListRecords("id",
                "partyId",
                "shopId",
                dev.vality.fraudbusters.management.domain.enums.ListType.white,
                "test",
                "test",
                LocalDateTime.now(), null, null, LocalDateTime.now()));

        mockMvc.perform(delete("/payments-lists/{id}", test))
                .andExpect(status().isOk());

        List<ChangeCommand> eventList = new CopyOnWriteArrayList<>();
        testCommandKafkaConsumer.read(topicCommand, data -> eventList.add(data.value()));
        Awaitility.await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertEquals(1, eventList.size()));

        assertEquals(1, eventList.size());
        assertEquals(eventList.get(0).command, Command.DELETE);
        assertEquals(eventList.get(0).getRow().getListType(), ListType.white);
    }
}
