package ru.aloyenz.whilebot.exceptions.schema;

public class NotInitializedException extends RuntimeException {
    public NotInitializedException() {
        super("Не удалось создать начальную ветку домашки. Вы даже не открывали фигурных скобок?");
    }
}
