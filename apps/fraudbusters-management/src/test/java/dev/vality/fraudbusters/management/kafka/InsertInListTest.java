package dev.vality.fraudbusters.management.kafka;

import tools.jackson.databind.json.JsonMapper;
import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.fraudbusters.management.TestObjectFactory;
import dev.vality.fraudbusters.management.config.KafkaITest;
import dev.vality.fraudbusters.management.controller.ErrorController;
import dev.vality.fraudbusters.management.dao.payment.wblist.WbListDao;
import dev.vality.fraudbusters.management.domain.payment.PaymentListRecord;
import dev.vality.fraudbusters.management.domain.payment.request.ListRowsInsertRequest;
import dev.vality.fraudbusters.management.resource.payment.PaymentsListsResource;
import dev.vality.testcontainers.annotations.kafka.config.KafkaConsumer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@KafkaITest
public class InsertInListTest {

    private static final String VALUE = "value";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";

    @Value("${kafka.topic.wblist.command}")
    public String topicCommand;
    @MockitoBean
    public WbListDao wbListDao;
    @Autowired
    private KafkaConsumer<ChangeCommand> testCommandKafkaConsumer;

    private final JsonMapper objectMapper = JsonMapper.builder().build();

    @Autowired
    private PaymentsListsResource paymentsListsResource;

    private MockMvc mockMvc;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(paymentsListsResource, new ErrorController()).build();
    }

    @Test
    void insertToList() throws Exception {
        Mockito.doNothing().when(wbListDao).saveListRecord(any());
        PaymentListRecord record = new PaymentListRecord();
        record.setListName(LIST_NAME);
        record.setPartyId(PARTY_ID);
        record.setShopId(SHOP_ID);
        record.setValue(VALUE);
        PaymentListRecord recordSecond = new PaymentListRecord();
        recordSecond.setListName(LIST_NAME);
        recordSecond.setPartyId(PARTY_ID);
        recordSecond.setShopId(SHOP_ID);
        recordSecond.setValue(VALUE + 2);
        ListRowsInsertRequest listRowsInsertRequest = TestObjectFactory.testListRowsInsertRequest(record, recordSecond);
        mockMvc.perform(post("/payments-lists")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(listRowsInsertRequest)))
                .andExpect(status().isOk());

        List<ChangeCommand> eventList = new CopyOnWriteArrayList<>();
        testCommandKafkaConsumer.read(topicCommand, data -> eventList.add(data.value()));
        Awaitility.await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertEquals(2, eventList.size()));

        assertEquals(2, eventList.size());
        assertEquals(eventList.get(0).command, Command.CREATE);
        assertEquals(eventList.get(0).getRow().getListType(), ListType.black);
    }
}
