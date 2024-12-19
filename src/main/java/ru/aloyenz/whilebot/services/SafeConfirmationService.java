package ru.aloyenz.whilebot.services;

import ru.aloyenz.whilebot.exceptions.HandlerNotFoundException;
import ru.aloyenz.whilebot.services.safety.UserFingerprint;
import ru.aloyenz.whilebot.sql.utils.ConsumerWithSQLException;

import java.sql.SQLException;

public class SafeConfirmationService {

    public static final String FINGERPRINT_SEPARATOR = ";";

    private final ConfirmationService service = new ConfirmationService();


    public String addHandlerWithAutoCode(ConsumerWithSQLException<String> handler, UserFingerprint fingerprint, boolean simple, int length) {
        return service.addConfirmationHandler(service.generateUniqueCode(simple, length,
                fingerprint.toString() + FINGERPRINT_SEPARATOR) + FINGERPRINT_SEPARATOR
                + fingerprint, handler).split(FINGERPRINT_SEPARATOR)[0];
    }

    public String addHandlerWithAutoCode(ConsumerWithSQLException<String> handler, UserFingerprint fingerprint) {
        return addHandlerWithAutoCode(handler, fingerprint, false, 4);
    }

    public void handle(String code, UserFingerprint fingerprint) throws HandlerNotFoundException, SQLException {
        service.handle(code + FINGERPRINT_SEPARATOR + fingerprint);
    }
}
