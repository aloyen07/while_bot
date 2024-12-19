package ru.aloyenz.whilebot.services;

import ru.aloyenz.whilebot.exceptions.HandlerNotFoundException;
import ru.aloyenz.whilebot.sql.utils.ConsumerWithSQLException;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Random;
import java.util.function.Consumer;

public class ConfirmationService {

    public static String NUMBERS = "1234567890";
    public static String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    public static String ALL = NUMBERS + CHARS;

    private final HashMap<String, ConsumerWithSQLException<String>> handlers = new HashMap<>();

    public String generateUniqueCode(boolean simple, int length, String prefix) {
        StringBuilder code = new StringBuilder();
        boolean flag = true;
        Random random = new Random();

        while (flag) {
            for (int i = 0; i < length; i++) {
                if (simple) {
                    code.append(NUMBERS.charAt(random.nextInt(NUMBERS.length())));
                } else {
                    code.append(ALL.charAt(random.nextInt(ALL.length())));
                }
            }

            if (!handlers.containsKey(prefix + code)) {
                flag = false;
            }
        }

        return code.toString();
    }

    public String addHandlerWithAutoCode(ConsumerWithSQLException<String> handler, boolean simple, int length) {
        String code = generateUniqueCode(simple, length, "");
        addConfirmationHandler(code, handler);
        return code;
    }

    public String addHandlerWithAutoCode(ConsumerWithSQLException<String> handler) {
        return addHandlerWithAutoCode(handler, true, 4);
    }

    public String addConfirmationHandler(String code, ConsumerWithSQLException<String> handler) {
        handlers.put(code, handler);
        return code;
    }

    public void handle(String code) throws HandlerNotFoundException, SQLException {
        ConsumerWithSQLException<String> handler = handlers.get(code);

        if (handler != null) {
            handler.accept(code);
            handlers.remove(code);
        } else {
            throw new HandlerNotFoundException();
        }
    }
}
