package gscript.factory.date;

import gscript.Factory;
import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.text.SimpleDateFormat;
import java.util.Date;

public final class GroovyDateFactory {

    private final Factory factory;

    public Date createDate(Object year, Object monthOfYear, Object dayOfMonth) {
        return new LocalDate(
                Integer.parseInt(year.toString()),
                Integer.parseInt(monthOfYear.toString()),
                Integer.parseInt(dayOfMonth.toString())
        ).toDate();
    }

    public Date createDate(Object year, Object monthOfYear, Object dayOfMonth, Object hourOfDay, Object minuteOfHour) {
        return new DateTime(
                Integer.parseInt(year.toString()),
                Integer.parseInt(monthOfYear.toString()),
                Integer.parseInt(dayOfMonth.toString()),
                Integer.parseInt(hourOfDay.toString()),
                Integer.parseInt(minuteOfHour.toString())
        ).toDate();
    }

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
     * Получить текущую дату (без времени)
     */
    public Date getCurrentDate() {
        return new LocalDate().toDate();
    }

    /**
     * Получить текущую дату с прибавленными днями
     *
     * @param days прибавить к текущей дате столько дней (-days - отнять дни)
     */
    public Date getCurrentDatePlus(int days) {
        return new LocalDate().plusDays(days).toDate();
    }

    /**
     * Получить текущую дату и время
     */
    public Date getCurrentDateTime() {
        return new DateTime().toDate();
    }

    /**
     * Прибавить/отнять к дате кол-во дней
     *
     * @param date дата
     * @param days кол-во дней (если с минусом - отнимет)
     * @return
     */
    public Date plusDays(Date date, int days) {
        return new DateTime(date).plusDays(days).toDate();
    }

    /**
     * Прибавить/отнять к дате кол-во месяцев
     *
     * @param date   дата
     * @param months кол-во месяцев (если с минусом - отнимет)
     * @return
     */
    public Date plusMonths(Date date, int months) {
        return new DateTime(date).plusMonths(months).toDate();
    }

    /**
     * Удалить милисекунды из даты
     *
     * @param date дата
     */
    public Date withoutMillis(Date date) {
        return new DateTime(date).withMillisOfSecond(0).toDate();
    }

    /**
     * Удалить время из даты
     *
     * @param date
     */
    public Date withoutTime(Date date) {
        return new LocalDate(date).toDate();
    }

    public Date parseDate(String str, String dateFormat) throws Exception {
        if ("".equals(str.trim()))
            return null;

        return new SimpleDateFormat(dateFormat).parse(str);
    }

    public String getDateAsString(Date date) {
        final DateTime dateTime = new DateTime(date);
        if (dateTime.getHourOfDay() == 0 && dateTime.getMinuteOfHour() == 0 && dateTime.getSecondOfMinute() == 0)
            return dateTime.toString("dd.MM.yyyy");
        else
            return dateTime.toString("dd.MM.yyyy HH:mm:ss");
    }

}
