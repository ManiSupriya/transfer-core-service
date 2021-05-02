package com.mashreq.transfercoreservice.paylater.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public class DateUtil {

    private static DateFormat getDateFormatter() {
        DateFormat dateFormatter = new SimpleDateFormat("dd-MM-yyyy");
        dateFormatter.setLenient(false);
        return dateFormatter;
    }

    public static String currentDateAsString() {
        return getDateFormatter().format(Date.from(LocalDate.now().atStartOfDay()
                .atZone(ZoneId.systemDefault()).toInstant()));
    }

    public static String currentDateTimeAsString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd_HH_mm"));
    }

    public static String currentLongDateAsString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    public static String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }

    public static String formatDate(LocalDateTime dateTime, String pattern) {
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"));
    }

    public static String currentShortDateAsString() {
        return LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyMMddHHmmss"));
    }

    public static String subtractMonthFromCurrentDate(int daysToSubtract, String format) {
        Instant now = Instant.now();
        Instant yesterday = now.minus(daysToSubtract, ChronoUnit.DAYS);
        return dateToString(Date.from(yesterday), format);
    }

    public static String dateToString(Date date, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(date);
    }
}
