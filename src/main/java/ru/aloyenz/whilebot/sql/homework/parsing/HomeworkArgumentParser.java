package ru.aloyenz.whilebot.sql.homework.parsing;

import ru.aloyenz.whilebot.exceptions.RecordNotFoundException;
import ru.aloyenz.whilebot.sql.homework.Lesson;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Стандартные настроечные значения:
 * Name // 2021.11.11   // 1d      // История
 * Name // 2d 1p        // 1234    // Ист
 * Имя  // Время оконч. // ret.ded // Предмет
 */
public class HomeworkArgumentParser {

    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d+(?=y)");
    private static final Pattern MONTH_PATTERN = Pattern.compile("\\d+(?=mo)");
    private static final Pattern DAY_PATTERN = Pattern.compile("\\d+(?=d)");
    private static final Pattern HOUR_PATTERN = Pattern.compile("\\d+(?=h)");
    private static final Pattern MINUTE_PATTERN = Pattern.compile("\\d+(?=m)");
    private static final Pattern SECOND_PATTERN = Pattern.compile("\\d+(?=s)");
    private static final Pattern PAIR_PATTERN = Pattern.compile("\\d+(?=p)");

    private static final List<Pattern> PATTERNS = List.of(YEAR_PATTERN, MONTH_PATTERN, DAY_PATTERN, HOUR_PATTERN,
            MINUTE_PATTERN, SECOND_PATTERN, PAIR_PATTERN);

    public static HomeworkArgument parseString(String in) throws ParseException, RecordNotFoundException, SQLException {
        String[] parts = in.split("//", 4);

        if (parts.length != 4 && parts.length != 3) {
            throw new RuntimeException("Некорректное количество частей заголовка. Ожидалось 4 или 3 части, найдено " + parts.length);
        }

        for (int i = 0; i < parts.length; i++) {
            parts[i] = parts[i].strip();
            if (parts[i].isBlank()) {
                throw new RuntimeException("Аргумент под номером " + (i + 1) + " пуст!");
            }
        }

        String name = parts[0];

        Timestamp end;
        try {
            end = parseTimestamp(parts[1], false);
        } catch (RuntimeException e) {
            throw new RuntimeException("Не удалось прочитать время окончания!");
        }

        int offset = 0;
        long retakeDeadline;
        if (parts.length != 3) {
            try {
                retakeDeadline = end.getTime() / 1000 - parseTimestamp(parts[2], true).getTime() / 1000;
            } catch (RuntimeException e) {
                throw new RuntimeException("Не удалось прочитать время окончания периода отмены!");
            }

            if (retakeDeadline < 0) {
                throw new RuntimeException("Время отказа не может быть отрицательным.");
            }
        } else {
            offset = 1;
            retakeDeadline = end.getTime() / 1000 - parseTimestamp("1d", true).getTime() / 1000;
        }



        Lesson lesson;
        int lessonIDRaw = -1;

        try {
            lessonIDRaw = Integer.parseInt(parts[3 - offset]);
        } catch (NumberFormatException ignore) {}

        if (lessonIDRaw != -1) {
            lesson = Lesson.lessonFor(lessonIDRaw);
        } else {
            lesson = Lesson.lessonFor(parts[3 - offset]);
        }

        return new HomeworkArgument(name, end, lesson.getId(), Timestamp.from(Instant.ofEpochSecond(retakeDeadline)));
    }

    private static Timestamp parseTimestamp(String time, boolean isForDeadline) throws ParseException {
        if (hasAnyMatchWithUser(time)) { // USER format
            String[] split = time.split(" ", 2);
            if (!hasAnyMatchWithUser(split[0]) && split.length == 2) { // We have overrides
                String overrides = split[0];
                boolean daysFlag = false;
                boolean hourFlag = false;

                if (overrides.contains("d")) {
                    daysFlag = true;
                } else if (overrides.contains("h")) {
                    hourFlag = true;
                }

                return parseTimestampFromUser(split[1].strip(), isForDeadline, daysFlag, hourFlag);
            } else {
                return parseTimestampFromUser(time, isForDeadline, false, false);
            }
        } else { // DATETIME format
            return parseTimestampFromTime(time.substring(2));
        }

    }

    private static boolean hasAnyMatchWithUser(String in) {
        boolean match = false;
        for (Pattern pattern : PATTERNS) {
            if (pattern.matcher(in).find()) {
                match = true;
            }
        }

        return match;
    }

    @SuppressWarnings("deprecation")
    private static Timestamp parseTimestampFromTime(String time) throws ParseException {
        try {
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            Date parsedDate = dateFormat.parse(time);
            return new Timestamp(parsedDate.getTime());
        } catch (ParseException e) {
            try {
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd hh:mm:ss");
                Date parsedDate = dateFormat.parse(time);
                return new Timestamp(parsedDate.getTime());
            } catch (ParseException ex) {
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("hh:mm:ss");
                    Date parsedDate = dateFormat.parse(time);
                    Date now = Date.from(Instant.now());
                    parsedDate.setYear(now.getYear());
                    parsedDate.setMonth(now.getMonth());
                    parsedDate.setDate(now.getDate());

                    return new Timestamp(parsedDate.getTime());
                } catch (ParseException exc) {
                    try {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date parsedDate = dateFormat.parse(time);
                        return new Timestamp(parsedDate.getTime());
                    } catch (ParseException except) {
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy.MM.dd");
                        Date parsedDate = dateFormat.parse(time);
                        return new Timestamp(parsedDate.getTime());
                    }
                }
            }
        }
    }

    private static int getByPattern(String time, Pattern pattern) {
        Matcher matcher = pattern.matcher(time);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group());
        } else {
            return 0;
        }
    }

    private static Timestamp parseTimestampFromUser(String time, boolean isForDeadline, boolean overrideDays, boolean overrideHours) {
        Calendar cal = Calendar.getInstance();
        if (!isForDeadline) {
            cal.setTime(Date.from(Instant.now()));
        } else {
            cal.setTime(new Date(0));
        }
        if (overrideDays) {
            cal.set(Calendar.YEAR, getByPattern(time, YEAR_PATTERN));
            cal.set(Calendar.MONTH, getByPattern(time, MONTH_PATTERN));
            cal.set(Calendar.DAY_OF_MONTH, getByPattern(time, DAY_PATTERN));
        } else {
            cal.add(Calendar.YEAR, getByPattern(time, YEAR_PATTERN));
            cal.add(Calendar.MONTH, getByPattern(time, MONTH_PATTERN));
            cal.add(Calendar.DAY_OF_MONTH, getByPattern(time, DAY_PATTERN));
        }

        if (overrideHours) {
            cal.set(Calendar.HOUR_OF_DAY, getByPattern(time, HOUR_PATTERN));
        } else {
            cal.add(Calendar.HOUR_OF_DAY, getByPattern(time, HOUR_PATTERN));
        }

        if (isForDeadline || !PAIR_PATTERN.matcher(time).find()) {
            if (overrideHours) {
                cal.set(Calendar.MINUTE, getByPattern(time, MINUTE_PATTERN));
                cal.set(Calendar.SECOND, getByPattern(time, SECOND_PATTERN));
            } else {
                cal.add(Calendar.MINUTE, getByPattern(time, MINUTE_PATTERN));
                cal.add(Calendar.SECOND, getByPattern(time, SECOND_PATTERN));
            }
        } else { // Lesson format
            cal.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(LessonSchema.values()[getByPattern(time, PAIR_PATTERN) - 1].getTime().split(":")[0]));
            cal.set(Calendar.MINUTE,
                    Integer.parseInt(LessonSchema.values()[getByPattern(time, PAIR_PATTERN) - 1].getTime().split(":")[1]));
            cal.set(Calendar.SECOND, 0);
        }
        return new Timestamp(cal.getTime().getTime());
    }
}
