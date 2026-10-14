package dev.vality.fraudbusters.management.kafka;

import dev.vality.damsel.wb_list.*;
import dev.vality.fraudbusters.management.config.KafkaITest;
import dev.vality.fraudbusters.management.dao.payment.wblist.WbListDao;
import dev.vality.fraudbusters.management.domain.payment.PaymentCountInfo;
import dev.vality.fraudbusters.management.domain.payment.PaymentListRecord;
import dev.vality.fraudbusters.management.domain.payment.request.ListRowsInsertRequest;
import dev.vality.fraudbusters.management.domain.tables.pojos.WbListRecords;
import dev.vality.fraudbusters.management.service.iface.AuditService;
import dev.vality.fraudbusters.management.utils.MethodPaths;
import dev.vality.fraudbusters.management.utils.TestKafkaProducer;
import dev.vality.swag.fraudbusters.management.model.ListResponse;
import dev.vality.testcontainers.annotations.kafka.config.KafkaConsumer;
import org.apache.thrift.TBase;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.security.autoconfigure.actuate.web.servlet.ManagementWebSecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.jooq.autoconfigure.JooqAutoConfiguration;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.security.autoconfigure.web.servlet.ServletWebSecurityAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@KafkaITest
@AutoConfigureTestRestTemplate
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EnableAutoConfiguration(exclude = {
        FlywayAutoConfiguration.class,
        JooqAutoConfiguration.class,
        ManagementWebSecurityAutoConfiguration.class,
        SecurityAutoConfiguration.class,
        ServletWebSecurityAutoConfiguration.class, OAuth2ResourceServerAutoConfiguration.class})
public class WbListApplicationTest {

    public static final String BASE_URL = "http://localhost:";
    private static final String VALUE = "value";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";
    @Value("${kafka.topic.wblist.event.sink}")
    public String topicEventSink;
    @Value("${kafka.topic.wblist.command}")
    public String topicCommand;
    @MockitoBean
    public AuditService auditService;
    @MockitoBean
    public WbListDao wbListDao;
    @Autowired
    private TestRestTemplate restTemplate;
    String paymentListPath;
    @LocalServerPort
    private int port;

    @Autowired
    private TestKafkaProducer<TBase<?, ?>> testKafkaProducer;

    @Autowired
    private KafkaConsumer<ChangeCommand> testCommandKafkaConsumer;

    @BeforeEach
    void init() {
        paymentListPath = String.format(MethodPaths.SERVICE_BASE_URL + MethodPaths.INSERT_PAYMENTS_LIST_ROW_PATH,
                port);
    }

    @Test
    void listenCreated() throws ExecutionException, InterruptedException {
        Mockito.clearInvocations(wbListDao);

        Event event = new Event();
        Row row = createRow(ListType.black);
        event.setRow(row);
        event.setEventType(EventType.CREATED);
        testKafkaProducer.send(topicEventSink, event);
        testKafkaProducer.send(topicEventSink, event);
        testKafkaProducer.send(topicEventSink, event);

        await().untilAsserted(() -> {
            Mockito.verify(wbListDao, Mockito.times(3)).saveListRecord(any());
        });
    }

    @Test
    void listenCreatedGrey() {
        Mockito.clearInvocations(wbListDao);

        Event event = new Event();
        Row row = createRow(ListType.grey);
        event.setEventType(EventType.CREATED);
        RowInfo rowInfo = RowInfo.count_info(new CountInfo()
                .setCount(5)
                .setTimeToLive("2019-08-22T13:14:17.443332Z")
                .setStartCountTime("2019-08-22T11:14:17.443332Z")
        );
        row.setRowInfo(rowInfo);

        event.setRow(row);

        testKafkaProducer.send(topicEventSink, event);
        testKafkaProducer.send(topicEventSink, event);
        testKafkaProducer.send(topicEventSink, event);

        await().untilAsserted(() -> {
            Mockito.verify(wbListDao, Mockito.times(3)).saveListRecord(any(WbListRecords.class));
        });
    }

    @Test
    void listenDeleted() {
        Event event = new Event();
        Row row = createRow(ListType.black);
        event.setRow(row);
        event.setEventType(EventType.DELETED);
        testKafkaProducer.send(topicEventSink, event);

        await().untilAsserted(() -> {
            Mockito.verify(wbListDao, Mockito.times(1)).removeRecord(any(WbListRecords.class));
        });

    }

    private Row createRow(ListType listType) {
        Row row = new Row();
        row.setPartyId(PARTY_ID);
        row.setShopId(SHOP_ID);
        row.setListName(LIST_NAME);
        row.setListType(listType);
        row.setValue(VALUE);
        return row;
    }

    @Test
    void createListRow() {
        String value = VALUE + 66;
        HttpEntity<ListRowsInsertRequest> entity =
                new HttpEntity<>(createListRowsInsertRequest(value), new org.springframework.http.HttpHeaders());
        ResponseEntity<String> response = restTemplate.exchange(paymentListPath, HttpMethod.POST, entity, String.class);
        assertTrue(response.getStatusCode().is2xxSuccessful(), response.toString());

        List<ChangeCommand> eventList = new CopyOnWriteArrayList<>();
        testCommandKafkaConsumer.read(topicCommand, data -> eventList.add(data.value()));
        Awaitility.await().atMost(Duration.ofSeconds(30))
                .untilAsserted(() -> assertEquals(1, eventList.size()));

        assertEquals(1, eventList.size());
        assertEquals(eventList.get(0).command, Command.CREATE);
        assertEquals(eventList.get(0).getRow().getListType(), ListType.black);
        assertEquals(value, eventList.get(0).getRow().getValue());

        String paymentListNames = String.format(MethodPaths.SERVICE_BASE_URL + MethodPaths.LISTS_NAMES_LIST_TYPE_PATH,
                port);
        ResponseEntity<ListResponse> result =
                restTemplate.getForEntity(paymentListNames, ListResponse.class, "black");
        assertTrue(result.getBody().getResult().isEmpty());
    }

    private ListRowsInsertRequest createListRowsInsertRequest(String value) {
        ListRowsInsertRequest listRowsInsertRequest = new ListRowsInsertRequest();
        listRowsInsertRequest.setListType(ListType.black);
        PaymentListRecord listRecord = new PaymentListRecord();
        listRecord.setListName(LIST_NAME);
        listRecord.setPartyId(PARTY_ID);
        listRecord.setShopId(SHOP_ID);
        listRecord.setValue(value);
        PaymentCountInfo paymentCountInfo = new PaymentCountInfo();
        paymentCountInfo.setListRecord(listRecord);
        listRowsInsertRequest.setRecords(List.of(paymentCountInfo));
        return listRowsInsertRequest;
    }
}
