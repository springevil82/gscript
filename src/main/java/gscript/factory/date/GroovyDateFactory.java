package gscript.factory.date;

import gscript.Factory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class GroovyDateFactory {

    private final Factory factory;

    /**
     * Create date
     *
     * @param year        year (int or string)
     * @param monthOfYear month of year (int or string)
     * @param dayOfMonth  day of month (int or string)
     * @return date
     */
    public Date createDate(Object year, Object monthOfYear, Object dayOfMonth) {
        return new LocalDate(
                Integer.parseInt(year.toString()),
                Integer.parseInt(monthOfYear.toString()),
                Integer.parseInt(dayOfMonth.toString())
        ).toDate();
    }

    /**
     * Create date
     *
     * @param year year (int or string)
     * @param monthOfYear month of year (int or string)
     * @param dayOfMonth day of month (int or string)
     * @param hourOfDay hour of day (int or string)
     * @param minuteOfHour minute of hour (int or string)
     * @return date
     */
    public Date createDate(Object year, Object monthOfYear, Object dayOfMonth, Object hourOfDay, Object minuteOfHour) {
        return new DateTime(
                Integer.parseInt(year.toString()),
                Integer.parseInt(monthOfYear.toString()),
                Integer.parseInt(dayOfMonth.toString()),
                Integer.parseInt(hourOfDay.toString()),
                Integer.parseInt(minuteOfHour.toString())
        ).toDate();
    }

    /**
     * Create date
     *
     * @param year year (int or string)
     * @param monthOfYear month of year (int or string)
     * @param dayOfMonth day of month (int or string)
     * @param hourOfDay hour of day (int or string)
     * @param minuteOfHour minute of hour (int or string)
     * @param secondOfMinute second of minute (int or string)
     * @return date
     */
    public Date createDate(Object year, Object monthOfYear, Object dayOfMonth, Object hourOfDay, Object minuteOfHour, Object secondOfMinute) {
        return new DateTime(
                Integer.parseInt(year.toString()),
                Integer.parseInt(monthOfYear.toString()),
                Integer.parseInt(dayOfMonth.toString()),
                Integer.parseInt(hourOfDay.toString()),
                Integer.parseInt(minuteOfHour.toString()),
                Integer.parseInt(secondOfMinute.toString())
        ).toDate();
    }

    public GroovyDateFactory(Factory factory) {
        this.factory = factory;
    }

    /**
     * Get current date (without time)
     */
    public Date getCurrentDate() {
        return new LocalDate().toDate();
    }

    /**
     * Returns a current date (without time) plus the specified number of days.
     *
     * @param days the amount of days to add, may be negative
     */
    public Date getCurrentDatePlus(int days) {
        return new LocalDate().plusDays(days).toDate();
    }

    /**
     * Get current date (with time)
     */
    public Date getCurrentDateTime() {
        return new DateTime().toDate();
    }

    /**
     * Add amount of days to specified date (with time)
     *
     * @param date date
     * @param days the amount of days to add, may be negative
     * @return new date
     */
    public Date plusDays(Date date, int days) {
        return new DateTime(date).plusDays(days).toDate();
    }

    /**
     * Add amount of months to specified date (with time)
     *
     * @param date   date
     * @param months the amount of months to add, may be negative
     * @return new date
     */
    public Date plusMonths(Date date, int months) {
        return new DateTime(date).plusMonths(months).toDate();
    }

    /**
     * Remove millis from date
     *
     * @param date date
     */
    public Date withoutMillis(Date date) {
        return new DateTime(date).withMillisOfSecond(0).toDate();
    }

    /**
     * Remove time from datetime
     *
     * @param date datetime
     */
    public Date withoutTime(Date date) {
        return new LocalDate(date).toDate();
    }

    /**
     * Parse given date (as string) using dateFormat
     * @param str date as string
     * @param dateFormat date format
     * @return date
     * @throws Exception
     */
    public Date parseDate(String str, String dateFormat) throws Exception {
        if ("".equals(str.trim()))
            return null;

        return new SimpleDateFormat(dateFormat).parse(str);
    }

    /**
     * Convert given date to string using date format dd.MM.yyyy by default
     * @param date date
     * @return date as string
     */
    public String getDateAsString(Date date) {
        final DateTime dateTime = new DateTime(date);
        if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0)
            return dateTime.toString("dd.MM.yyyy");
        else
            return dateTime.toString("dd.MM.yyyy HH:mm:ss");
    }

}
