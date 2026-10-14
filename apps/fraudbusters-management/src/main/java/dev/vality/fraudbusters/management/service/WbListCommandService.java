package dev.vality.fraudbusters.management.service;

import dev.vality.damsel.wb_list.*;
import dev.vality.fraudbusters.management.exception.KafkaProduceException;
import dev.vality.fraudbusters.management.exception.SaveRowsException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TBase;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class WbListCommandService {

    private final KafkaTemplate<String, TBase> kafkaTemplate;
    @Value("${kafka.topic.wblist.command}")
    public String topicCommand;

    public String sendCommandSync(Row row, ListType type, Command command, String initiator) {
        row.setListType(type);
        var uuid = UUID.randomUUID().toString();
        try {
            var changeCommand = createChangeCommand(row, command, initiator);
            kafkaTemplate.send(topicCommand, uuid, changeCommand)
                    .get();
            log.info("WbListCommandService sent command: {}", changeCommand);
        } catch (InterruptedException e) {
            log.error("InterruptedException e: ", e);
            Thread.currentThread().interrupt();
            throw new KafkaProduceException(e);
        } catch (Exception e) {
            log.error("Error when send e: ", e);
            throw new KafkaProduceException(e);
        }
        return uuid;
    }

    private ChangeCommand createChangeCommand(Row row, Command command, String initiator) {
        return new ChangeCommand()
                .setRow(row)
                .setCommand(command)
                .setUserInfo(new UserInfo()
                        .setUserId(initiator));
    }

    public <T> List<String> sendListRecords(List<T> records,
                                            ListType listType,
                                            BiFunction<T, ListType, Row> func,
                                            Command command,
                                            String initiator) {
        try {
            return records.stream()
                    .map(record -> func.apply(record, listType))
                    .map(row -> {
                        log.info("WbListResource list add row {}", row);
                        return sendCommandSync(row, listType, command, initiator);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error when insert rows: {} e: ", records, e);
            throw new SaveRowsException(e);
        }
    }

}
