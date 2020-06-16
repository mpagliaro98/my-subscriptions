package com.mpagliaro98.mysubscriptions.model;

import java.util.Calendar;
import java.util.Date;

/**
 * An object that functions like a specialized Calendar object. When created, this acts like
 * a Calendar object set to the current date, but with the invariant that all fields related
 * to time (hour, minute, second, millisecond) are set to 0. So if this is created on April
 * 5th, the calendar will hold the date April 5th at the time 0:00:00. This is useful for
 * comparing the Date objects used throughout the application to today's date, and setting
 * the time to zero allows us to have a common grounds for comparison.
 */
public class ZeroTimeCalendar {

    // The internal Calendar object
    private Calendar calendar;

    /**
     * Create the calendar, initializing it's time to today at the time 0:00:00.
     */
    public ZeroTimeCalendar() {
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Get the current date information held by the calendar.
     * @return a Date object of today set to 0:00:00.
     */
    public Date getCurrentDate() {
        return calendar.getTime();
    }

    /**
     * Get the day of the month this calendar is set to.
     * @return the day of the month as an int
     */
    public int getDayOfMonth() {
        return calendar.get(Calendar.DAY_OF_MONTH);
    }

    /**
     * Get the month this calendar is set to.
     * @return the month as an int
     */
    public int getMonth() {
        return calendar.get(Calendar.MONTH);
    }

    /**
     * Get the year this calendar is set to.
     * @return the year as an int
     */
    public int getYear() {
        return calendar.get(Calendar.YEAR);
    }

    /**
     * Increment the day this calendar is storing by a given amount. Pass in a negative
     * value to decrement it.
     * @param days the number of days to modify by
     */
    public void addDays(int days) {
        calendar.add(Calendar.DATE, days);
    }

    /**
     * Increment the month this calendar is storing by a given amount. Pass in a negative
     * value to decrement it.
     * @param months the number of months to modify by
     */
    public void addMonths(int months) {
        calendar.add(Calendar.MONTH, months);
    }

    /**
     * Increment the year this calendar is storing by a given amount. Pass in a negative
     * value to decrement it.
     * @param years the number of months to modify by
     */
    public void addYears(int years) {
        calendar.add(Calendar.YEAR, years);
    }

    /**
     * Set the date this calendar holds to a given day, month, and year. The time at this
     * date will stay at 0:00:00.
     * @param year the year to change to
     * @param monthOfYear the month to change to
     * @param dayOfMonth the day of the month to change to
     */
    public void setTime(int year, int monthOfYear, int dayOfMonth) {
        calendar.set(year, monthOfYear, dayOfMonth);
    }

    /**
     * Set the date of this calendar by passing in a Date object, and the calendar will
     * set itself to the given day in the Date object. No matter what time of day the given
     * Date object has, the calendar will stay at 0:00:00 on that date.
     * @param date a Date object to set the current date to
     */
    public void setTimeToDate(Date date) {
        calendar.setTimeInMillis(date.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Clone this ZeroTimeCalendar and return the copy of it. The original object is
     * not changed, and modifying the copy has no effect on the original.
     * @return a copy of this calendar object
     */
    public ZeroTimeCalendar copyCalendar() {
        ZeroTimeCalendar newCalendar = new ZeroTimeCalendar();
        newCalendar.setTimeToDate(this.getCurrentDate());
        return newCalendar;
    }
}
