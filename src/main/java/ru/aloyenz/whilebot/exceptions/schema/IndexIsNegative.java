package ru.aloyenz.whilebot.exceptions.schema;

public class IndexIsNegative extends RuntimeException {
    public IndexIsNegative(RawIndexIsNegative exception, int line) {
        super("Нельзя создавать ветки с индексом ниже или равным нулю. Сейчас обнаружен индекс " + exception.getMessage() + " на строке " + line);
    }
}
