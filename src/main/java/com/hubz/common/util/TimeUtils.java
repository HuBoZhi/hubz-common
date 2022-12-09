package com.hubz.common.util;

import cn.hutool.core.date.DateUtil;
import org.apache.commons.lang3.time.DateUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Date;

/**
 * @author hubz
 * @date 2021/8/24 21:37
 **/
public final class TimeUtils {

    private TimeUtils() {
    }

    /**
     * yyyy-MM-dd 格式化时间的长度
     */
    private static final Integer TEN = 10;
    /**
     * 时区
     */
    private static final ZoneId ZONE = ZoneId.systemDefault();
    /**
     * 格式化的格式
     */
    public static final String DEFAULT_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String SIMPLE_TIME_FORMAT = "yyyyMMddHHmmss";
    public static final String SIMPLE_DATE_FORMAT = "yyyy-MM-dd";

    /**
     * 时间戳转化为格式化时间
     * @author hubz
     * @date 2020/7/23 15:51
     * @param longTime 时间戳:单位 秒
     * @param format 格式化形式：类似 yyyy-MM-dd
     * @return String 格式化时间
     **/
    public static String convertTimeToString(long longTime, String format) {
        return convertMillTimeToString(longTime * DateUtils.MILLIS_PER_SECOND, format);
    }

    /**
     * 时间戳转化为格式化时间
     * @author hubz
     * @date 2020/7/23 15:51
     * @param longTime 时间戳:单位 毫秒
     * @return 格式化的时间
     **/
    public static String convertMillTimeToString(long longTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_TIME_FORMAT);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(longTime), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * date转化为格式化时间
     * @param date Date日期对象
     * @return java.lang.String
     *
     * @author hubz
     * @date 2021/9/18 22:38
     */
    public static String convertDateToString(Date date) {
        Long longTime = date2Long(date);
        return convertMillTimeToString(longTime);
    }


    /**
     * 时间戳转化为格式化时间
     * @author hubz
     * @date 2020/7/23 15:51
     * @param longTime 时间戳:单位 毫秒
     * @param format 格式化形式：类似 yyyy-MM-dd
     * @return 格式化的时间
     **/
    public static String convertMillTimeToString(long longTime, String format) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(format);
        LocalDateTime localDateTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(longTime), ZoneId.systemDefault());
        return localDateTime.format(dateTimeFormatter);
    }

    /**
     * 将格式化时间转化为时间戳
     * @author hubz
     * @date 2020/7/23 16:09
     * @param dateTime 格式化时间
     * @param format 应该转化的格式
     * @return long
     **/
    public static long convertTimeToLong(String dateTime, String format) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern(format);
        if (dateTime.length() != format.length()) {
            throw new Exception(String.format("%s 和 %s 格式不一致", dateTime, format));
        }
        if (dateTime.length() > TEN) {
            // 年月日时分秒
            LocalDateTime localDateTime = LocalDateTime.parse(dateTime, dtf);
            return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        } else {
            //年月日
            LocalDate localDate = LocalDate.parse(dateTime, dtf);
            return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
        }
    }

    /**
     * 返回当前时间的时间戳
     * @author hubz
     * @date 2020/7/23 16:02
     * @return long 毫秒
     **/
    public static long getCurrentLongTime() {
        return Instant.now().toEpochMilli();
    }

    /**
     * 返回格式化时间
     * @author hubz
     * @date 2020/7/23 16:35
     * @param format 格式
     * @return 返回当前时间的格式化时间
     **/
    public static String getCurrentTime(String format) {
        return convertMillTimeToString(getCurrentLongTime(), format);
    }

    /**
     * 获取当前的格式化时间
     * @return java.lang.String yyyy-MM-dd HH:mm:ss
     *
     * @author hubz
     * @date 2021/8/24 21:47
     */
    public static String getCurrentFormatTimeString() {
        return getCurrentTime(DEFAULT_TIME_FORMAT);
    }

    public static String getCurrentSimpleFormatTimeString() {
        return getCurrentTime(SIMPLE_TIME_FORMAT);
    }

    /**
     * date转为LocalTime
     * @author hubz
     * @date 2021/3/30 17:09
     **/
    public static LocalTime date2LocalTime(Date date) {
        Instant instant = date.toInstant();
        return instant.atZone(ZONE).toLocalTime();
    }

    /**
     * 获取当前小时数
     * @return java.lang.Integer 当前小时数
     *
     * @author hubz
     * @date 2022/1/1 14:59
     */
    public static Integer getCurrentHour() {
        Instant now = Instant.now();
        LocalTime localTime = now.atZone(ZONE).toLocalTime();
        return localTime.getHour();
    }

    /**
     * date转Long时间戳
     *
     * @author hubz
     * @date 2021/9/18 22:34
     */
    public static Long date2Long(Date date) {
        Instant instant = date.toInstant();
        return instant.toEpochMilli();
    }

    /**
     * 将LocalDateTime转为date
     * @author hubz
     * @date 2021/3/30 17:09
     *
     * @param localDateTime LocalDateTime日期对象
     * @return Date
     **/
    public static Date localTime2Date(LocalDateTime localDateTime) {
        Instant instant = localDateTime.atZone(ZONE).toInstant();
        return Date.from(instant);
    }

    public static Long localTime2Long(LocalDateTime localDateTime) {
        return localDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static Date long2Date(Long timestamp) {
        return new Date(timestamp);
    }

    /**
     * 获取昨日的格式化时间
     * @param format 格式化字符串
     * @return java.lang.String
     *
     * @author hubz
     * @date 2022/2/19 16:18
     */
    public static String getYesterdayFormatTimeString(String format) {
        Long dateLong = getYesterdayStartTimestamp();
        return convertMillTimeToString(dateLong, format);
    }

    /**
     * 获取今日0点的时间戳
     * @return java.lang.Long
     *
     * @author hubz
     * @date 2022/2/19 16:21
     */
    public static Long getCurrentDayStartTimestamp() {
        ZonedDateTime zonedDateTime = LocalDate.now().atStartOfDay(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 获取昨日0点的时间戳
     * @return java.lang.Long
     *
     * @author hubz
     * @date 2022/2/19 16:21
     */
    public static Long getYesterdayStartTimestamp() {
        ZonedDateTime zonedDateTime = LocalDate.now().minusDays(1).atStartOfDay(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 获得本周一0点时间戳
     * @return java.lang.Long
     *
     * @author hubz
     * @date 2022/2/19 16:29
     */
    public static Long getCurrentWeekStartTimestamp() {
        LocalDate localDate = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        ZonedDateTime zonedDateTime = localDate.atStartOfDay(ZoneId.systemDefault());
        return zonedDateTime.toInstant().toEpochMilli();
    }

    /**
     * 获取本周日23:59:59的时间戳
     * @return java.lang.Long
     *
     * @author hubz
     * @date 2022/2/19 16:48
     */
    public static Long getCurrentWeekEndTimestamp() {
        LocalDate localDate = LocalDate.now().with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        return LocalDateTime.of(localDate, LocalTime.MAX).toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    /**
     * 尝试将字符串转为Date对象，如果失败则返回当前时间Date
     * @author hubz
     * @date 2022/4/20 9:21
     *
     * @param dateFormat 格式化时间
     * @return java.util.Date
     **/
    public static Date tryStrToDate(String dateFormat) {
        try {
            return DateUtil.parse(dateFormat);
        } catch (Exception e) {
            return new Date();
        }
    }
}
