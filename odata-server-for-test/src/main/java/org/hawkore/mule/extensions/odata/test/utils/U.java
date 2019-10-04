package org.hawkore.mule.extensions.odata.test.utils;

import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;

import static org.slf4j.LoggerFactory.getLogger;

/**
 * Some utilities
 *
 * @author Manuel Núñez Sánchez (manuel.nunez@hawkore.com)
 */
public class U {

    private static final Logger LOGGER = getLogger(U.class);

    private U() {}

    /**
     * To zoned date time.
     *
     * @param temporal
     *     the temporal
     * @return the zoned date time
     */
    public static ZonedDateTime toZonedDateTime(Calendar temporal) {
        if (temporal == null) {
            return null;
        }
        TimeZone tz = temporal.getTimeZone();
        ZoneId zid = tz == null ? ZoneOffset.UTC : tz.toZoneId();
        return ZonedDateTime.ofInstant(temporal.toInstant(), zid);
    }

    /**
     * To local date time.
     *
     * @param temporal
     *     the temporal
     * @return the local date time
     */
    public static LocalDateTime toLocalDateTime(Date temporal) {
        if (temporal == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTime(temporal);
        calendar.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        return LocalDateTime.ofInstant(calendar.toInstant(), ZoneOffset.UTC);
    }

    /**
     * To local date time.
     *
     * @param temporal
     *     the temporal
     * @return the local date time
     */
    public static LocalDateTime toLocalDateTime(Timestamp temporal) {
        if (temporal == null) {
            return null;
        }
        return temporal.toLocalDateTime();
    }

    /**
     * To local time.
     *
     * @param temporal
     *     the temporal
     * @return the local time
     */
    public static LocalTime toLocalTime(Time temporal) {
        if (temporal == null) {
            return null;
        }
        return temporal.toLocalTime();
    }

    /**
     * To timestamp.
     *
     * @param temporal
     *     the temporal
     * @return the timestamp
     */
    public static Timestamp toTimestamp(LocalDateTime temporal) {
        if (temporal == null) {
            return null;
        }
        return Timestamp.valueOf(temporal);
    }

    /**
     * To calendar.
     *
     * @param temporal
     *     the temporal
     * @return the calendar
     */
    public static Calendar toCalendar(OffsetDateTime temporal) {
        if (temporal == null) {
            return null;
        }
        return toCalendar(temporal.toZonedDateTime());
    }

    /**
     * To calendar.
     *
     * @param temporal
     *     the temporal
     * @return the calendar
     */
    public static Calendar toCalendar(ZonedDateTime temporal) {
        if (temporal == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(temporal.getYear(), temporal.getMonthValue() - 1, temporal.getDayOfMonth(), temporal.getHour(),
            temporal.getMinute(), temporal.getSecond());
        calendar.setTimeZone(TimeZone.getTimeZone(temporal.getZone()));
        calendar.set(Calendar.MILLISECOND, Double.valueOf((1d * temporal.getNano()) / 1_000_000d).intValue());
        return calendar;
    }

    /**
     * To date.
     *
     * @param temporal
     *     the temporal
     * @return the date
     */
    public static Date toDate(LocalDate temporal) {
        if (temporal == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.setTimeZone(TimeZone.getTimeZone(ZoneOffset.UTC));
        // assuming start of day
        calendar.set(temporal.getYear(), temporal.getMonthValue() - 1, temporal.getDayOfMonth());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    /**
     * To calendar calendar.
     *
     * @param temporal
     *     the temporal
     * @return the calendar
     */
    public static Calendar toCalendar(OffsetTime temporal) {
        if (temporal == null) {
            return null;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        //assuming year/month/date information from epoch (January 1, 1970.)
        calendar.set(1970, 0, 1, temporal.getHour(), temporal.getMinute(), temporal.getSecond());
        calendar.setTimeZone(TimeZone.getTimeZone(temporal.getOffset()));
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    /**
     * To time time.
     *
     * @param temporal
     *     the temporal
     * @return the time
     */
    public static Time toTime(LocalTime temporal) {
        if (temporal == null) {
            return null;
        }
        return Time.valueOf(temporal);
    }

}
