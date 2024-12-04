package ru.aloyenz.whilebot.exceptions.schema;

public class OrdinalLevelNonNull extends RuntimeException {
    public OrdinalLevelNonNull(int ordinal) {
        super("Уровень вложенности неравен нулю (равен " + ordinal + "). У вас есть незакрытые/лишние фигурные скобки?");
    }
}
