package ru.aloyenz.whilebot.exceptions.schema;

public class OrdinalLevelIsNegative extends RuntimeException {
    public OrdinalLevelIsNegative(int ordinal) {
        super("Отрицательный уровень вложенности: " + ordinal + ". У вас лишние закрывающие скобки?");
    }
}
