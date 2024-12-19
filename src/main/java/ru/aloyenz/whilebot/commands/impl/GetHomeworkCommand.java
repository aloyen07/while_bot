package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.exceptions.MessagedException;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.sql.homework.Homework;
import ru.aloyenz.whilebot.sql.homework.Lesson;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

public class GetHomeworkCommand extends Command {

    public GetHomeworkCommand() {
        super("Get Homework Command");
    }

    @Override
    public int argsNum() {
        return -1;
    }

    @Override
    public String usage(boolean isShort) {
        return "+get_homework [lesson] {ID} - Получить домашнюю работу. Если не указан аргумент ID, будет выдан список всех валидных работ.";
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> {

            if (args.isEmpty()) {
                return new Message().setText("Недостаточное количество аргументов!");
            }

            Lesson lesson;
            try {
                try {
                    int id = Integer.parseInt(args.get(0));
                    lesson = Lesson.lessonFor(id);
                } catch (NumberFormatException ignore) {
                    lesson = Lesson.lessonFor(args.get(0));
                }
            } catch (RecordNotFoundException e) {
                return new Message().setText("Предмет с ID или именем " + args.get(0) + " не найден!");
            }

            if (args.size() == 1) {
                List<Homework> homeworks = Homework.getHomeworksForLesson(lesson.getId())
                        .stream().filter(homework ->
                                homework.getEndsAt().after(Timestamp.from(Instant.now())) && !homework.isClosed()
                        ).toList();
                StringBuilder message = new StringBuilder("Для предмета ").append(lesson.getName()).append(" найдено ").append(homeworks.size()).append(" домашних работ.\n\nСписок:");

                for (Homework homework : homeworks) {
                    message.append("\n").append(homework.getId()).append(" - ").append(homework.getName()).append("\n");
                    message.append(" * Добавлено: ").append(homework.getCreatedAt().toString()).append("\n");
                    message.append(" * Истекает: ").append(homework.getEndsAt().toString()).append("\n");
                    message.append(" * Запрещен отказ от работы после: ").append(homework.getRetakeDeadline()).append("\n\n");
                }

                return new Message().setText(message.toString());
            } else {
                int index;

                try {
                    index = Integer.parseInt(args.get(1));
                } catch (NumberFormatException e) {
                    return new Message().setText("Неверный аргумент под позицией ID!");
                }

                Homework homework;
                try {
                    homework = Homework.getByID(index);
                } catch (RecordNotFoundException e) {
                    return new Message().setText("Не удалось найти данную домашнюю работу");
                }

                try {
                    homework.getBranchSchemaFromDatabase();
                } catch (RecordNotFoundException e) {
                    logger.error("An error has been occurred while processing the GetHomework command!");
                    logger.error("INDEX: {}", index);
                    logger.throwing(Level.ERROR, e);
                    return new Message().setText("Возникла внутренняя ошибка. Обратитесь к администратору.");
                }

                TreeBranch branch = homework.getMainTreeBranch();

                if (branch == null) {
                    return new Message().setText("Домашняя работа не найдена!");
                }

                String message = "Домашняя работа.\n" + "\nID: " + homework.getId() + " - " + homework.getName() + "\n" +
                        " * Добавлено: " + homework.getCreatedAt().toString() + "\n" +
                        " * Истекает: " + homework.getEndsAt().toString() + "\n" +
                        " * Запрещен отказ от работы после: " + homework.getRetakeDeadline() + "\n\n" +
                        "Содержимое домашней работы: \n\n" + branch +
                        "Напишите команду +take [ID домашней работы] [подпункт] для взятия темы";

                return new Message().setText(message);
            }
        };
    }

    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of();
    }
}
