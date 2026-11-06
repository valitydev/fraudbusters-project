package dev.vality.wb.list.manager.service;

import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.damsel.wb_list.Event;
import dev.vality.damsel.wb_list.EventType;
import dev.vality.wb.list.manager.converter.CommandToRowConverter;
import dev.vality.wb.list.manager.model.Row;
import dev.vality.wb.list.manager.repository.ListRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommandServiceImpl implements CommandService {

    private final CommandToRowConverter commandToRowConverter;
    private final ListRepository listRepository;

    @Override
    public Event apply(ChangeCommand command) {
        log.info("CommandService apply command: {}", command);
        Row row = commandToRowConverter.convert(command);
        log.info("CommandService apply row: {}", row);
        if (row == null || !StringUtils.hasText(row.getValue())) {
            log.warn("Ignore empty command row value: {}", row);
            return null;
        }
        Event event = applyCommandAndGetEvent(command, row);
        event.setRow(command.getRow());
        event.setUserInfo(command.getUserInfo());
        return event;
    }

    private Event applyCommandAndGetEvent(ChangeCommand command, Row row) {
        return switch (command.getCommand()) {
            case CREATE -> {
                listRepository.create(row);
                yield new Event().setEventType(EventType.CREATED);
            }
            case DELETE -> {
                listRepository.remove(row);
                yield new Event().setEventType(EventType.DELETED);
            }
            default -> {
                log.warn("CommandService command for list not found! command: {}", command);
                throw new RuntimeException("WbListStreamFactory command for list not found!");
            }
        };
    }

}
