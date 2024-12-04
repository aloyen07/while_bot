package ru.aloyenz.whilebot.exceptions.schema;

public class LineEndRequired extends RuntimeException {
    public LineEndRequired(int line) {
        super("Ожидается \";\" перед символом \"}\", находящемуся на линии " + line);
    }
}
