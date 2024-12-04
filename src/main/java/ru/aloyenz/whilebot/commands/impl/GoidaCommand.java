package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.util.List;

public class GoidaCommand extends Command {

    public GoidaCommand() {
        super("Goida Command");
    }

    @Override
    public int argsNum() {
        return -1;
    }

    @Override
    public String usage(boolean isShort) {
        return "";
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> new Message().setText("ГОЙДА!!!");
    }

    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of(PermissionType.ADD);
    }
}
