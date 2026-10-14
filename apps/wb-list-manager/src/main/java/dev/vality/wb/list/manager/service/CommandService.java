package dev.vality.wb.list.manager.service;

import dev.vality.damsel.wb_list.ChangeCommand;
import dev.vality.damsel.wb_list.Event;

public interface CommandService {

    Event apply(ChangeCommand command);

}
