package dev.vality.fraudbusters.management.utils;

import dev.vality.damsel.fraudbusters.Command;
import dev.vality.damsel.fraudbusters.CommandType;
import dev.vality.damsel.fraudbusters.UserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandMapper {

    private final UserInfoService userInfoService;

    public Command mapToConcreteCommand(String userName, final Command command, CommandType commandType) {
        Command newCommand = new Command(command);
        newCommand.setCommandType(commandType)
                .setUserInfo(new UserInfo()
                        .setUserId(userName)
                );
        return newCommand;
    }

}
