package ru.aloyenz.whilebot.exceptions.schema;

public class IndexParseError extends RuntimeException {
    public IndexParseError(RawIndexParseError error, int line) {
        super("Не удалось привести индекс к числу (перевести строку \"" + error.getMessage() + "\" в число) на строке " + line);
    }
}
