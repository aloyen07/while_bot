package ru.aloyenz.whilebot.exceptions.schema;

public class BracketsMismatch extends RuntimeException {

    private final String message;

    public BracketsMismatch(int opens, int closes) {
        if (opens > closes) {
            message = "Ожидалось \"}\" для закрытия подпункта.";
        } else {
            message = "Ожидалось \"{\" для открытия подпункта (или есть лишнее \"}\").";
        }
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return message;
    }
}
