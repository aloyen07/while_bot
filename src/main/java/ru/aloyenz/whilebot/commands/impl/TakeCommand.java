package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.sql.homework.Homework;
import ru.aloyenz.whilebot.sql.homework.schema.Branch;
import ru.aloyenz.whilebot.sql.homework.schema.BranchEndpoint;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.util.List;

public class TakeCommand extends Command {

    public TakeCommand() {
        super("Take Command");
    }

    @Override
    public int argsNum() {
        return 2;
    }

    @Override
    public String usage(boolean isShort) {
        return "+take [ID домашней работы] [пунктВыбора]\n\nПример: +take Ист 8 2.1.1\nБерет домашнюю работу по истории под ID 8 с пунктом 2.1.1";
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> {
            Homework homework;
            try {
                homework = Homework.getByID(Integer.parseInt(args.get(0)));
            } catch (RecordNotFoundException e) {
                return new Message().setText("Не удалось найти домашнюю работу с этим ID");
            } catch (NumberFormatException e) {
                return new Message().setText("В качестве аргумента для ID работы должно быть только число!");
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
                return new Message().setText("Не удалось найти структуру домашней работы");
            }

            if (branch.hasTookAny(Math.toIntExact(context.message().getObject().getMessage().getFromId()))) {
                return new Message().setText("Вы уже взяли эту работу!");
            }

            StringBuilder path = new StringBuilder();
            Branch b = branch;
            for (String rawIndex : args.get(1).split("\\.")) {
                path.append(rawIndex).append(".");
                int index;
                try {
                    index = Integer.parseInt(rawIndex);
                } catch (NumberFormatException e) {
                    return new Message().setText("Неверное указание пункта темы! Получено: " + rawIndex + ", когда ожидалось число! " + getPath(path));
                }

                if (b instanceof TreeBranch treeBranch) {
                    b = treeBranch.getBranchByID(index);
                } else {
                    return new Message().setText("Некорректное указание темы (в пути указана конечная ветка). " + getPath(path));
                }

                if (b == null) {
                    return new Message().setText("Ветка не найдена! " + getPath(path));
                }
            }

            if (b instanceof TreeBranch) {
                return new Message().setText("Это подветка (не конечная ветка). Её нельзя выбрать!");
            }

            BranchEndpoint endpoint = (BranchEndpoint) b;
            if (endpoint.getTookID() != 0) {
                return new Message().setText("Эта тема уже выбрана!");
            }

            endpoint.setTookID(Math.toIntExact(context.message().getObject().getMessage().getFromId()));
            return new Message().setText("Тема " + endpoint.getName() + " успешно выбрана!");
        };
    }

    private String getPath(StringBuilder rawPath) {
        return "Подпуть: " + rawPath.substring(0, rawPath.toString().length() - 1);
    }

    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of(PermissionType.USE);
    }
}
