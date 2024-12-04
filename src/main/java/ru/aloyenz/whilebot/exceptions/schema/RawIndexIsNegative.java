package ru.aloyenz.whilebot.exceptions.schema;

public class RawIndexIsNegative extends Throwable {
    public RawIndexIsNegative(int index) {
        super("" + index);
    }
}
