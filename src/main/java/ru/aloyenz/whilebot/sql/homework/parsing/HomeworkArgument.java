package ru.aloyenz.whilebot.sql.homework.parsing;

import java.sql.Timestamp;

public record HomeworkArgument(String name, Timestamp endsAt, int lessonID, Timestamp retakeDeadline) {
}
