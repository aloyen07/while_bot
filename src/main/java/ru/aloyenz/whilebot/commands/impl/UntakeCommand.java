package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.sql.homework.Homework;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;
import ru.aloyenz.whilebot.sql.permissions.Permission;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class UntakeCommand extends Command {

    public UntakeCommand() {
        super("Untake Command");
    }

    @Override
    public int argsNum() {
        return 1;
    }

    @Override
    public String usage(boolean isShort) {
        return "+untake [ID домашней работы] - отменить взятие темы";
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> {

            int index;
            try {
                index = Integer.parseInt(args.get(0));
            } catch (NumberFormatException e) {
                return new Message().setText("Не удалось распознать число на месте ID!");
            }

            Homework homework;
            try {
                homework = Homework.getByID(index);
            } catch (RecordNotFoundException e) {
                return new Message().setText("Не удалось найти домашнюю работу!");
            }

            try {
                homework.getBranchSchemaFromDatabase();
            } catch (RecordNotFoundException e) {
                logger.error("An error has been occurred while processing the GetHomework command!");
                logger.error("INDEX: {}", args.get(0));
                logger.throwing(Level.ERROR, e);
                return new Message().setText("Возникла внутренняя ошибка. Обратитесь к администратору.");
            }

            TreeBranch branch = homework.getMainTreeBranch();
            if (branch == null) {
                return new Message().setText("Не удалось найти сигнатуру работы!");
            }

            if (branch.hasTookAny(Math.toIntExact(context.message().getObject().getMessage().getFromId()))) {
                if (!homework.getRetakeDeadline().after(Timestamp.from(Instant.now()))) {
                    if (!Permission.forMessageNewAuthor(context.message()).hasPermission(PermissionType.FORCE_RETAKE)) {
                        return new Message().setText("Время на отказ истекло!");
                    }
                }

                branch.setTookID(Math.toIntExact(context.message().getObject().getMessage().getFromId()), 0);
                return new Message().setText("Вы успешно отказались от домашней работы!");
            } else {
                return new Message().setText("Вы не брали ничего по этой работе!");
            }
        };
    }

    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of(PermissionType.USE);
    }
}
