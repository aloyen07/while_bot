package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.exceptions.HandlerNotFoundException;
import ru.aloyenz.whilebot.services.safety.UserFingerprint;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.util.List;

public class ConfirmCommand extends Command {

    public ConfirmCommand() {
        super("Confirm command");
    }

    @Override
    public int argsNum() {
        return 1;
    }

    @Override
    public String usage(boolean isShort) {
        return "+confirm [code] - Подтверждает то или иное действие.";
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> {
            try {
                Main.getConfirmationService().handle(args.get(0), new UserFingerprint(context.message()));
            } catch (HandlerNotFoundException e) {
                return new Message().setText("Не удалось найти данный код");
            }

            return new Message().setText("Успешно подтверждено!");
        };
    }

    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of();
    }
}
