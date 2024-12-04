package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import com.vk.api.sdk.objects.users.responses.GetResponse;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.sql.permissions.Permission;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.util.ArrayList;
import java.util.List;

public class RevokeCommand extends Command {

    public RevokeCommand() {
        super("Revoke Command");
    }

    @Override
    public int argsNum() {
        return 2;
    }

    @Override
    public String usage(boolean isShort) {
        if (isShort) {
            return "+revoke [userPing/ID] [nums] - Забирает привилегии у пользователя. [nums] разделены точками с запятой";
        } else {
            StringBuilder builder = new StringBuilder("+revoke [userPing/ID] [nums] - Забирает привилегии у пользователя. [nums] разделены точками с запятой");

            builder.append("\n\nПривилегии в nums:\n");
            for (PermissionType type : PermissionType.values()) {
                builder.append(type.getPos()).append(" - ").append(type.getName()).append(" - ").append(type.getDescription());
            }

            return builder.toString();
        }
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> {
            String user = args.get(0);
            String nums = args.get(1);

            List<GetResponse> responses = context.client().users().get(context.actor())
                    .userIds(user.replace("@", "").replace("*", "")
                            .split("\\|")[0].replace("[", "").replace("id", "")).execute();

            if (responses.isEmpty()) {
                return new Message().setText("Пользователь с ID " + user + " не найден!");
            }

            List<String> unparsed = new ArrayList<>();

            List<PermissionType> types = new ArrayList<>();
            for (String num : nums.split(";")) {
                try {
                    PermissionType type = PermissionType.getFromID(Integer.parseInt(num));
                    if (type == null) {
                        unparsed.add(num);
                    } else {
                        types.add(type);
                    }
                } catch (NumberFormatException ignore) {
                    return null;
                }
            }

            Permission permission = Permission.forMessageNewAuthor(context.message());
            permission.removePermission(types);

            StringBuilder builder = new StringBuilder();
            if (!unparsed.isEmpty()) {
                builder.append("Не удалось забрать привилегии с номерами ");
                for (String id : unparsed) {
                    builder.append(id).append(" ");
                }
                builder.append("\n");
            }

            if (!types.isEmpty()) {
                builder.append("Забраны привилегии с номерами ");
                for (PermissionType type : types) {
                    builder.append(type.getPos()).append(" ");
                }
            }

            if (types.isEmpty() && unparsed.isEmpty()) {
                builder.append("Не удалось ничего сделать");
            }

            return new Message().setText(builder.toString());
        };
    }

    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of(PermissionType.ADMINISTRATOR);
    }
}
