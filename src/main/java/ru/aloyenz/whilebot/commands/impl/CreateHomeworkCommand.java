package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.Main;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.services.safety.UserFingerprint;
import ru.aloyenz.whilebot.sql.homework.Homework;
import ru.aloyenz.whilebot.sql.homework.Lesson;
import ru.aloyenz.whilebot.sql.homework.parsing.HomeworkArgument;
import ru.aloyenz.whilebot.sql.homework.parsing.HomeworkArgumentParser;
import ru.aloyenz.whilebot.sql.homework.parsing.Parser;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;
import ru.aloyenz.whilebot.sql.utils.Pair;

import java.util.List;

public class CreateHomeworkCommand extends Command {

    public CreateHomeworkCommand() {
        super("CreateHomework Command");
    }

    @Override
    public int argsNum() {
        return -1;
    }

    @Override
    public String usage(boolean isShort) {
        if (isShort) return "+create_homework [content] - создает домашнюю работу";
        else return """
+create_homework [content] - создает домашнюю работу

Синтаксис домашней работы:
Название ДЗ {
  Подпункт {
    Задание 1;
    Задание 2;
    Задание 3;
  }
  Задание 4;
}

И подпункт и задание имеют следующий вид:
[Номер] - [Имя]//[Описание];
Пример: 2 - Что такое ОРГ?//Расскажите, что для вас означает это буквосочетание.
Номер - обязательная часть для каждого этого типа. Имя и описание - нет.

Разделение происходит по точке с запятой или же по фигурной скобочке (поэтому, если вам требуется сделать точку с запятой, вам придется использовать экранирование.
Финальный номер задания вычисляется по всем номерам подпунктов, в которое оно вложено.
""";
    }

    @Override
    public CommandExecutor executor() {
        return (context, args) -> {
            // Parsing a homework
            try {
                Pair<String, TreeBranch> pair = Parser.createHomeworkFromString(context.message().getObject().getMessage().getText());
                HomeworkArgument argument;
                try {
                    argument = HomeworkArgumentParser.parseString(pair.first().split(" ", 2)[1]);
                } catch (RecordNotFoundException e) {
                    return new Message().setText("Не удалось найти домашнюю работу по этому имени/индексу");
                }

                String code = Main.getConfirmationService().addHandlerWithAutoCode((codeIn) -> {
                    Homework.createHomework(argument.name(), pair.second(), argument.endsAt(), argument.retakeDeadline(), argument.lessonID());
                }, new UserFingerprint(context.message()));

                StringBuilder builder = new StringBuilder("Необходимо убедиться, что вы верно добавили домашнюю работу...\n\n");

                builder.append("Предмет: ").append(Lesson.lessonFor(argument.lessonID())).append("\n");
                builder.append("Дата окончания: ").append(argument.endsAt().toString()).append("\n");
                builder.append("Дата окончания периода отказа: ").append(argument.retakeDeadline().toString()).append("\n\n");

                builder.append("Сигнатура:\n");
                builder.append(pair.second().toString().strip()).append("\n\n");

                builder.append("Для подтверждения отправьте +confirm ").append(code).append("\n");
                builder.append("Для отказа отправьте +cancel ").append(code).append("\n");

                return new Message().setText(builder.toString());
            } catch (RuntimeException exception) {
                return new Message().setText(exception.getMessage());
            } catch (Exception e) {
                logger.error("Unable to parse a homework.");
                logger.error("Message: \n{}", context.message().getObject().getMessage().getText());
                logger.error("Stacktrace:");
                logger.throwing(Level.ERROR, e);
                return new Message().setText("Неизвестная ошибка, обратитесь к администратору");
            }
        };
    }



    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of(PermissionType.ADD);
    }
}
