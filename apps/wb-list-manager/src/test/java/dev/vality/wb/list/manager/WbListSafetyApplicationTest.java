package dev.vality.wb.list.manager;

import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.ListType;
import dev.vality.damsel.wb_list.Row;
import dev.vality.testcontainers.annotations.KafkaSpringBootTest;
import dev.vality.testcontainers.annotations.kafka.KafkaTestcontainer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducer;
import dev.vality.wb.list.manager.config.ConsumerConfig;
import dev.vality.wb.list.manager.exception.DbExecutionException;
import dev.vality.wb.list.manager.repository.ListRepository;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.flyway.autoconfigure.FlywayAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@KafkaSpringBootTest
@TestPropertySource(properties = {"retry.timeout=100"})
@KafkaTestcontainer(topicsKeys = {"kafka.wblist.topic.command", "kafka.wblist.topic.event.sink"})
@Import(ConsumerConfig.class)
@EnableAutoConfiguration(exclude = FlywayAutoConfiguration.class)
public class WbListSafetyApplicationTest {

    @Value("${kafka.wblist.topic.command}")
    public String topic;

    @Autowired
    private KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;

    @MockitoBean
    private ListRepository listRepository;

    @Test
    void kafkaRowTestException() throws Exception {
        doThrow(new DbExecutionException(),
                new DbExecutionException())
                .doNothing()
                .when(listRepository).create(any());
        ChangeCommand changeCommand = TestObjectFactory.testCommand();
        changeCommand.setCommand(Command.CREATE);

        testThriftKafkaProducer.send(topic, changeCommand);

        verify(listRepository, timeout(5000L).times(3)).create(any());
    }

    @Test
    void kafkaRowTestEmptyValue() throws Exception {
        ChangeCommand changeCommand = TestObjectFactory.testCommand();
        changeCommand.setCommand(Command.CREATE);
        changeCommand.setRow(new Row()
                .setListType(ListType.black)
                .setShopId("test")
                .setValue("")
                .setListName("test"));
        testThriftKafkaProducer.send(topic, changeCommand);

        clearInvocations(listRepository);
        verify(listRepository, timeout(2000L).times(0)).create(any());
    }

}
