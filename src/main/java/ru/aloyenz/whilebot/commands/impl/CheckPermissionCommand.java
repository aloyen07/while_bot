package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.sql.permissions.Permission;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.util.List;

public class CheckPermissionCommand extends Command {

    public CheckPermissionCommand() {
        super("CheckPermission Command");
    }

    @Override
    public int argsNum() {
        return -1;
    }

    @Override
    public String usage(boolean isShort) {
        return "Показывает уровень Ваших привилегий.";
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> {
            StringBuilder builder = new StringBuilder("Ваш уровень привилегий: ");

            Permission permission = Permission.forMessageNewAuthor(context.message());
            builder.append(permission.getPermLevel());
            builder.append(" (ваш ID: ").append(context.message().getObject().getMessage().getFromId())
                    .append(").\n\nБолее подробные привилегии:\n");

            for (PermissionType type : PermissionType.values()) {
                builder.append(type.getName()).append(" - ").append(permission.hasPermission(type)).append(".\n");
            }

            return new Message().setText(builder.toString());
        };
    }

    @Override
    public @Nullable List<PermissionType> needPermission() {
        return null;
    }
}
