package ru.aloyenz.whilebot.exceptions;

public class RecordNotFoundException extends Exception {
    public RecordNotFoundException() {
        super();
    }

    public RecordNotFoundException(String name, String by, Object arg) {
        super("Could not find record with schema \"" + name + "\" by " + by + " with object " + arg);
    }


}
