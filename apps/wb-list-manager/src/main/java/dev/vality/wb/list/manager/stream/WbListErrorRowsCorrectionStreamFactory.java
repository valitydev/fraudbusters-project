package dev.vality.wb.list.manager.stream;

import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.damsel.wb_list.Command;
import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.EventType;
import dev.vality.wb.list.manager.exception.KafkaStreamInitialException;
import dev.vality.wb.list.manager.serializer.CommandSerde;
import dev.vality.wb.list.manager.serializer.EventSerde;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class WbListErrorRowsCorrectionStreamFactory {

    public static final String EMPTY_STRING = "";
    private final CommandSerde commandSerde = new CommandSerde();
    private final EventSerde eventSerde = new EventSerde();

    @Value("${kafka.wblist.topic.command}")
    private String readTopic;
    @Value("${kafka.wblist.topic.event.sink}")
    private String resultTopic;

    public KafkaStreams create(final Properties streamsConfiguration) {
        try {
            StreamsBuilder builder = new StreamsBuilder();
            builder.stream(resultTopic, Consumed.with(Serdes.String(), eventSerde))
                    .filter((s, event) -> isCreateCommandWithEmptyListName(event))
                    .peek((s, changeCommand) -> log.info("Command with empty list_name: {}", changeCommand))
                    .mapValues(this::invertCommandToDelete)
                    .to(readTopic, Produced.with(Serdes.String(), commandSerde));
            return new KafkaStreams(builder.build(), streamsConfiguration);
        } catch (Exception e) {
            log.error("WbListStreamFactory error when create stream e: ", e);
            throw new KafkaStreamInitialException(e);
        }
    }

    private ChangeCommand invertCommandToDelete(Event event) {
        ChangeCommand command = new ChangeCommand();
        command.setCommand(Command.DELETE);
        command.setRow(event.getRow());
        return command;
    }

    private boolean isCreateCommandWithEmptyListName(Event event) {
        return event.getRow() != null
                && EMPTY_STRING.equals(event.getRow().list_name)
                && event.getEventType() == EventType.CREATED;
    }

}
