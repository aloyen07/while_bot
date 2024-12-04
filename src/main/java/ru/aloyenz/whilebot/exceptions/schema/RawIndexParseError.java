package ru.aloyenz.whilebot.exceptions.schema;

public class RawIndexParseError extends Throwable {
    public RawIndexParseError(String index) {
        super(index);
    }
}
