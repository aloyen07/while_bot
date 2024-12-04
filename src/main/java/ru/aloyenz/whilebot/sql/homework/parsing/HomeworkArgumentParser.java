//package ru.aloyenz.whilebot.sql.homework.parsing;
//
//import java.sql.Timestamp;
//import java.text.ParseException;
//import java.text.SimpleDateFormat;
//import java.time.Instant;
//import java.util.Calendar;
//import java.util.Date;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
///**
// * Стандартные настроечные значения:
// * Name // t:2021.11.11 // u:1d    // История
// * Name // u:2d 1p      // d:1234  // Ист
// * Имя  // Время оконч. // ret.ded // Предмет
// */
//public class HomeworkArgumentParser {
//
//    private static final Pattern YEAR_PATTERN = Pattern.compile("\\d+(?=y)");
//    private static final Pattern MONTH_PATTERN = Pattern.compile("\\d+(?=mo)");
//    private static final Pattern DAY_PATTERN = Pattern.compile("\\d+(?=d)");
//    private static final Pattern HOUR_PATTERN = Pattern.compile("\\d+(?=h)");
//    private static final Pattern MINUTE_PATTERN = Pattern.compile("\\d+(?=m)");
//    private static final Pattern SECOND_PATTERN = Pattern.compile("\\d+(?=s)");
//
//    public static HomeworkArgument parseString(String in) {
//
//    }
//
//    private Timestamp parseTimestamp(String time, boolean hasPair) throws ParseException {
//        if (time.startsWith("t:")) {
//            return parseTimestampFromTime(time.substring(2));
//        } else if (time.startsWith("u:") && !hasPair) {
//            return parseTimestampFromUser(time);
//        }
//    }
//
//    private static Timestamp parseTimestampFromTime(String time) throws ParseException {
//        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
//        Date parsedDate = dateFormat.parse(time);
//        return new Timestamp(parsedDate.getTime());
//    }
//
//    private static int getByPattern(String time, Pattern pattern) {
//        Matcher matcher = pattern.matcher(time);
//        if (matcher.find()) {
//            return Integer.parseInt(matcher.group());
//        } else {
//            return 0;
//        }
//    }
//
//    private static Timestamp parseTimestampFromUser(String time) throws ParseException {
//        Calendar cal = Calendar.getInstance();
//        cal.setTime(Date.from(Instant.now()));
//        cal.add(Calendar.YEAR, getByPattern(time, YEAR_PATTERN));
//        cal.add(Calendar.MONTH, getByPattern(time, MONTH_PATTERN));
//        cal.add(Calendar.DAY_OF_MONTH, getByPattern(time, DAY_PATTERN));
//        cal.add(Calendar.HOUR_OF_DAY, getByPattern(time, HOUR_PATTERN));
//        cal.add(Calendar.MINUTE, getByPattern(time, MINUTE_PATTERN));
//        cal.add(Calendar.SECOND, getByPattern(time, SECOND_PATTERN));
//        return new Timestamp(cal.getTime().getTime());
//    }
//}
