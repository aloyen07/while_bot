package ru.aloyenz.whilebot.commands.impl;

import com.vk.api.sdk.objects.messages.Message;
import org.apache.logging.log4j.Level;
import org.jetbrains.annotations.Nullable;
import ru.aloyenz.whilebot.commands.Command;
import ru.aloyenz.whilebot.commands.CommandExecutor;
import ru.aloyenz.whilebot.exceptions.schema.*;
import ru.aloyenz.whilebot.sql.homework.Homework;
import ru.aloyenz.whilebot.sql.homework.parsing.Parser;
import ru.aloyenz.whilebot.sql.homework.schema.TreeBranch;
import ru.aloyenz.whilebot.sql.permissions.PermissionType;
import ru.aloyenz.whilebot.sql.utils.Pair;

import java.util.List;

public class CreateHomework extends Command {

    public CreateHomework() {
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

            } catch (BracketsMismatch | BranchIsEmpty | IndexDuplicationError | IndexIsNegative | IndexParseError
                    | LineEndRequired | NotInitializedException | OrdinalLevelIsNegative | OrdinalLevelNonNull exception) {
                return new Message().setText(exception.getMessage());
            } catch (Exception e) {
                logger.error("Unable to parse a homework.");
                logger.error("Message: \n{}", context.message().getObject().getMessage().getText());
                logger.error("Stacktrace:");
                logger.throwing(Level.ERROR, e);
                return new Message().setText("Неизвестная ошибка, обратитесь к администратору");
            }

            return new Message();
        };
    }



    @Override
    public @Nullable List<PermissionType> needPermission() {
        return List.of(PermissionType.ADD);
    }
}
