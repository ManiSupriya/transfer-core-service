package com.mashreq.transfercoreservice.paylater.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class DateUtilTest {

    @Test
    public void formatDate() {
        String date = DateUtil.currentDateTimeAsString();
        System.out.println(date);
        assertNotNull(date);
    }

    @Test
    public void currentDateAsString() {
        String date = DateUtil.currentDateAsString();
        System.out.println(date);
        assertNotNull(date);
    }

    @Test
    public void currentDateAsSeconds() {
        String date = DateUtil.formatDate(LocalDateTime.now(), "yyMMdd_HHmmss");
        System.out.println(date);
        assertNotNull(date);
    }
}