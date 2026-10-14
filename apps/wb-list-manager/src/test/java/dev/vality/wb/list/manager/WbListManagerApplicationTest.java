package dev.vality.wb.list.manager;

import dev.vality.damsel.wb_list.*;
import dev.vality.testcontainers.annotations.KafkaSpringBootTest;
import dev.vality.testcontainers.annotations.kafka.KafkaTestcontainer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaConsumer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducerConfig;
import dev.vality.wb.list.manager.config.ConsumerConfig;
import dev.vality.wb.list.manager.extension.AwaitilityExtension;
import dev.vality.wb.list.manager.utils.ChangeCommandWrapper;
import dev.vality.woody.api.flow.error.WRuntimeException;
import dev.vality.woody.thrift.impl.http.THClientBuilder;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.rnorth.ducttape.unreliables.Unreliables;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.testcontainers.shaded.com.trilead.ssh2.ChannelCondition.TIMEOUT;

@Import(ConsumerConfig.class)
@Testcontainers
@ExtendWith({AwaitilityExtension.class})
@KafkaSpringBootTest
@KafkaTestcontainer(topicsKeys = {"kafka.wblist.topic.command", "kafka.wblist.topic.event.sink"},
        properties = {"kafka.topic.payment.concurrency=1",
                "kafka.topic.payment.max-poll-records=1",
                "clean.period.day=0"})
@SpringBootTest(webEnvironment = RANDOM_PORT)
public class WbListManagerApplicationTest {

    public static final String IDENTITY_ID = "identityId";
    private static final String VALUE = "value";
    private static final String SHOP_ID = "shopId";
    private static final String PARTY_ID = "partyId";
    private static final String LIST_NAME = "listName";
    private static String SERVICE_URL = "http://localhost:%s/wb_list/v1";

    @Value("${kafka.wblist.topic.command}")
    public String topic;

    @Value("${kafka.wblist.topic.event.sink}")
    public String topicEventSink;

    @LocalServerPort
    int serverPort;

    @Autowired
    private KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;

    @Autowired
    private KafkaConsumer<Event> testCommandKafkaConsumer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private WbListServiceSrv.Iface handler;

    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgreSQLContainer<>("postgres:14-alpine");

    @DynamicPropertySource
    static void dataSourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgreSQLContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgreSQLContainer::getUsername);
        registry.add("spring.datasource.password", postgreSQLContainer::getPassword);
    }

    @BeforeEach
    void setUp() throws URISyntaxException {
        jdbcTemplate.execute("truncate table wb_list.row;");
        THClientBuilder clientBuilder = new THClientBuilder()
                .withAddress(new URI(String.format(SERVICE_URL, serverPort)))
                .withNetworkTimeout(300000);
        handler = clientBuilder.build(WbListServiceSrv.Iface.class);
    }

    @Test
    void kafkaStreamsTest() throws Exception {
        Row testRow = TestObjectFactory.testRow();
        ChangeCommand changeCommand = produceCreateRow(testRow);
        Awaitility.await()
                .until(() -> handler.isExist(changeCommand.getRow()));

        produceDeleteRow(changeCommand);

        boolean exist = handler.isExist(changeCommand.getRow());

        assertFalse(exist);

        List<Event> eventList = new ArrayList<>();
        testCommandKafkaConsumer.read(topicEventSink, data -> eventList.add(data.value()));
        Unreliables.retryUntilTrue(TIMEOUT, TimeUnit.SECONDS, () -> eventList.size() == 2);

        assertTrue(eventList.stream()
                .map(Event::getRow)
                .anyMatch(row -> row.getPartyId().equals(testRow.getPartyId())));
    }

    @Test
    void kafkaWbListCorrectionStreamsTest() throws Exception {
        Row testRow = TestObjectFactory.testRowWithEmptyListName();
        ChangeCommand changeCommand = produceCreateRow(testRow);
        handler.isExist(changeCommand.getRow());

        Awaitility.await()
                .atMost(Duration.ofSeconds(60))
                .pollDelay(2L, TimeUnit.SECONDS)
                .until(() -> !handler.isExist(changeCommand.getRow()));

        assertFalse(handler.isExist(changeCommand.getRow()));
    }

    @Test
    void kafkaRowTest() throws Exception {    // TODO refactoring
        Row row = createRowOld();
        ChangeCommand changeCommand = createCommand(row);
        row.setShopId(null);
        testThriftKafkaProducer.send(topic, changeCommand);
        Awaitility.await()
                .until(() -> handler.isExist(changeCommand.getRow()));


        row.setShopId(SHOP_ID);

        boolean exist = handler.isExist(row);

        assertTrue(exist);


        Result info = handler.getRowInfo(row);

        assertFalse(info.isSetRowInfo());


        row.setListType(ListType.grey);
        //check without partyId and shop id
        Row testRow = createRow(Instant.now().toString());
        RowInfo rowInfo = handler.getRowInfo(testRow).getRowInfo();

        assertEquals(5, rowInfo.getCountInfo().getCount());


        //check without partyId
        createRow(Instant.now().toString());
        rowInfo = handler.getRowInfo(row).getRowInfo();

        assertEquals(5, rowInfo.getCountInfo().getCount());


        //check full key field
        createRow(Instant.now().toString());
        rowInfo = handler.getRowInfo(row).getRowInfo();

        assertEquals(5, rowInfo.getCountInfo().getCount());


        rowInfo = checkCreateWithCountInfo(handler, Instant.now().toString());

        assertFalse(rowInfo.getCountInfo().getStartCountTime().isEmpty());


        produceDeleteRow(changeCommand);
        exist = handler.isExist(changeCommand.getRow());

        assertFalse(exist);
    }

    @Test
    public void kafkaRowP2PTest() throws InterruptedException {
        Row rowP2p = createListRow();
        rowP2p.setId(IdInfo.p2p_id(new P2pId().setIdentityId(IDENTITY_ID)));
        ChangeCommand p2pChangeCommand = produceCreateRow(rowP2p);
        assertThrows(WRuntimeException.class, () -> handler.isExist(p2pChangeCommand.getRow()));

        produceDeleteRow(p2pChangeCommand);
        assertThrows(WRuntimeException.class, () -> handler.isExist(p2pChangeCommand.getRow()));
    }

    private Row createRow(String startTimeCount) throws InterruptedException {
        ChangeCommand changeCommand;
        Row rowWithCountInfo = createRowWithCountInfo(startTimeCount);
        changeCommand = createCommand(rowWithCountInfo);
        testThriftKafkaProducer.send(topic, changeCommand);
        Thread.sleep(1000L);
        return rowWithCountInfo;
    }

    private ChangeCommand produceCreateRow(Row row)
            throws InterruptedException {
        ChangeCommand changeCommand = createCommand(row);
        testThriftKafkaProducer.send(topic, changeCommand);
        Thread.sleep(1000L);
        return changeCommand;
    }

    private void produceDeleteRow(ChangeCommand changeCommand)
            throws InterruptedException {
        changeCommand.setCommand(Command.DELETE);
        testThriftKafkaProducer.send(topic, changeCommand);
        Thread.sleep(1000L);
    }

    private Row createRowOld() {
        return createListRow()
                .setShopId(SHOP_ID)
                .setPartyId(PARTY_ID);
    }

    private ChangeCommandWrapper createCommand(Row row) {
        ChangeCommandWrapper changeCommand = new ChangeCommandWrapper();
        changeCommand.setCommand(Command.CREATE);
        changeCommand.setRow(row);
        return changeCommand;
    }

    private Row createRowWithCountInfo(String startTimeCount) {
        Row row = new Row();
        row.setId(IdInfo.payment_id(new PaymentId()
                .setShopId(SHOP_ID)
                .setPartyId(PARTY_ID)
        ));
        row.setListName(LIST_NAME);
        row.setListType(ListType.grey);
        row.setValue(VALUE);
        row.setRowInfo(RowInfo.count_info(
                new CountInfo()
                        .setCount(5L)
                        .setStartCountTime(startTimeCount)
                        .setTimeToLive(Instant.now().plusSeconds(6000L).toString()))
        );
        return row;
    }

    private RowInfo checkCreateWithCountInfo(WbListServiceSrv.Iface iface, String startTimeCount)
            throws InterruptedException, TException {
        Row rowWithCountInfo = createRow(startTimeCount);
        return iface.getRowInfo(rowWithCountInfo).getRowInfo();
    }

    private Row createListRow() {
        Row row = new Row();
        row.setListName(LIST_NAME);
        row.setListType(ListType.black);
        row.setValue(VALUE);
        return row;
    }

}
