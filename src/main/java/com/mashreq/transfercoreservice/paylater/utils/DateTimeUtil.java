package com.mashreq.transfercoreservice.paylater.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Slf4j
public enum DateTimeUtil {
    INSTANCE;

    private static final String SHORT_DATE_FMT = "yyyy-MM-dd";
    private static final String LONG_DATE_FMT = "yyyy-MM-dd HH:mm:ss";
    private static final String REPORT_DATE_FORMAT = "dd/MM/yyyy hh:mm:ss";
    private static final String BENE_MODIFICATION_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final ZoneId DUBAI_ZONE = ZoneId.of("GMT+04");
    private final DateFormat shortDateFmt = new SimpleDateFormat(SHORT_DATE_FMT);
    private final DateFormat longDateFmt = new SimpleDateFormat("yyyyMMddHHmmss");
    private final DateFormat reportDateFmt = new SimpleDateFormat(REPORT_DATE_FORMAT);
    public static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(SHORT_DATE_FMT);
    public static final DateTimeFormatter DATE_TIME_FORMATTER_ONE = DateTimeFormatter.ofPattern(BENE_MODIFICATION_DATE_FORMAT);
    public static DateTimeUtil getInstance() {
        return INSTANCE;
    }

    public boolean isBeforeCurrentDate(String value)  {
        try {
            Date date = shortDateFmt.parse(value);
            return date.before(new Date());
        } catch (Exception e) {
            return true;
        }
    }

    public String getCurrentDateTimeAsString() {
        return longDateFmt.format(new Date());
    }

    public String getReportDateAsString() {
        return reportDateFmt.format(new Date());
    }

    public String toString(Date date) {
        return shortDateFmt.format(date);
    }

    public long calculateNumberOfDays(LocalDate dateTime) {
        return ChronoUnit.DAYS.between(dateTime, LocalDate.now());
    }

    public LocalDate convertToDate(String date,DateTimeFormatter format ) {
        return LocalDate.parse(date, format);
    }
    
    public LocalDateTime convertToDateTime(String date, DateTimeFormatter formatter) {
    	return LocalDateTime.parse(date, formatter);
    }
    
    public static String getFormattedString(LocalDateTime dateTime) {
    	return dateTime.format(DATE_TIME_FORMATTER_ONE);
    }
    
	public static LocalDateTime getCurrentDateTimeZone() {
		return LocalDateTime.now();
	}
}
