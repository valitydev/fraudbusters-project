package dev.vality.wb.list.manager.utils;

import com.fasterxml.jackson.annotation.JsonFilter;
import dev.vality.damsel.wb_list.ChangeCommand;

@JsonFilter("myFilter")
public class ChangeCommandWrapper extends ChangeCommand {
}
