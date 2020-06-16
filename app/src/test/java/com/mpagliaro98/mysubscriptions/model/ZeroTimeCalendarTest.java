package com.mpagliaro98.mysubscriptions.model;

import org.junit.Before;
import org.junit.Test;

import java.util.Calendar;
import java.util.Date;
import static org.junit.Assert.assertEquals;

public class ZeroTimeCalendarTest {

    // The component under test
    private ZeroTimeCalendar CuT;

    private Calendar calendar;

    /**
     * Run before each test, initialize the component under test and a Calendar
     * object to test against.
     */
    @Before
    public void setup() {
        CuT = new ZeroTimeCalendar();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Test that the time kept by the calendar is always at 0:00:00.
     */
    @Test
    public void test_zero_time() {
        // Time not during daylight savings
        CuT.setTime(2020, 0, 1);
        Date date = CuT.getCurrentDate();
        long timeMillis = date.getTime();
        calendar.setTimeInMillis(timeMillis);
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));

        // Time during daylight savings
        CuT.setTime(2020, 5, 1);
        date = CuT.getCurrentDate();
        timeMillis = date.getTime();
        calendar.setTimeInMillis(timeMillis);
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    /**
     * Test retrieving the current date stored in the calendar.
     */
    @Test
    public void test_current_date() {
        CuT.setTime(2020, 0, 1);
        Date date = CuT.getCurrentDate();
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), date);
        assertEquals(1, CuT.getDayOfMonth());
        assertEquals(0, CuT.getMonth());
        assertEquals(2020, CuT.getYear());
    }

    /**
     * Test incrementing and decrementing the date by days, including when that value goes
     * over month and year boundaries.
     */
    @Test
    public void test_add_days() {
        CuT.setTime(2020, 0, 1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Increment by one day
        CuT.addDays(1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 2);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Return to the original day
        CuT.addDays(-1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Increment 31 days to get to the next month
        CuT.addDays(31);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Return to the original day
        CuT.addDays(-31);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Decrement by one to go back a year
        CuT.addDays(-1);
        calendar.set(Calendar.YEAR, 2019);
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 31);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Return to the original day
        CuT.addDays(1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
    }

    /**
     * Test incrementing and decrementing the date by months, including when that value goes
     * over year boundaries.
     */
    @Test
    public void test_add_months() {
        CuT.setTime(2020, 0, 1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        assertEquals(calendar.get(Calendar.DAY_OF_WEEK), CuT.getDayOfWeek());

        // Increment by one month
        CuT.addMonths(1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        assertEquals(calendar.get(Calendar.DAY_OF_WEEK), CuT.getDayOfWeek());

        // Return to the original month
        CuT.addMonths(-1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        assertEquals(calendar.get(Calendar.DAY_OF_WEEK), CuT.getDayOfWeek());

        // Decrement by one to go back a year
        CuT.addMonths(-1);
        calendar.set(Calendar.YEAR, 2019);
        calendar.set(Calendar.MONTH, 11);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        assertEquals(calendar.get(Calendar.DAY_OF_WEEK), CuT.getDayOfWeek());

        // Return to the original month
        CuT.addMonths(1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        assertEquals(calendar.get(Calendar.DAY_OF_WEEK), CuT.getDayOfWeek());

        // Increment by multiple months
        CuT.addMonths(6);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 6);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        assertEquals(calendar.get(Calendar.DAY_OF_WEEK), CuT.getDayOfWeek());
    }

    /**
     * Test incrementing and decrementing the date by years.
     */
    @Test
    public void test_add_years() {
        CuT.setTime(2020, 0, 1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Increment by one year
        CuT.addYears(1);
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Return to the original year
        CuT.addYears(-1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());

        // Increment by multiple years
        CuT.addYears(5);
        calendar.set(Calendar.YEAR, 2025);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
    }

    /**
     * Test setting the time using both explicit values and by passing in a Date object,
     * and ensure the invariant holds.
     */
    @Test
    public void test_set_time() {
        // Test using the set time method to change the date, but time stays at zero
        CuT.setTime(2020, 0, 1);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        calendar.setTimeInMillis(CuT.getCurrentDate().getTime());
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));

        CuT.setTime(2020, 1, 29);
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 29);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        calendar.setTimeInMillis(CuT.getCurrentDate().getTime());
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));

        // Test setting the time with a date object
        calendar.set(Calendar.YEAR, 2020);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        CuT.setTimeToDate(calendar.getTime());
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        calendar.setTimeInMillis(CuT.getCurrentDate().getTime());
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));

        // Set the time using a date that isn't zero time to verify the calendar stays at 0
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 25);
        calendar.set(Calendar.HOUR_OF_DAY, 19);
        calendar.set(Calendar.MINUTE, 1);
        calendar.set(Calendar.SECOND, 13);
        calendar.set(Calendar.MILLISECOND, 634);
        CuT.setTimeToDate(calendar.getTime());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        assertEquals(calendar.getTime(), CuT.getCurrentDate());
        calendar.setTimeInMillis(CuT.getCurrentDate().getTime());
        assertEquals(0, calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(0, calendar.get(Calendar.MINUTE));
        assertEquals(0, calendar.get(Calendar.SECOND));
        assertEquals(0, calendar.get(Calendar.MILLISECOND));
    }

    /**
     * Test creating a copy of the existing zero time calendar object.
     */
    @Test
    public void test_copy_calendar() {
        // Ensure the copy has all the same fields
        CuT.setTime(2020, 0, 1);
        ZeroTimeCalendar copy = CuT.copyCalendar();
        assertEquals(CuT.getDayOfMonth(), copy.getDayOfMonth());
        assertEquals(CuT.getDayOfWeek(), copy.getDayOfWeek());
        assertEquals(CuT.getMonth(), copy.getMonth());
        assertEquals(CuT.getYear(), copy.getYear());

        // A copy should be its own object, so it shouldn't change when the original is modified
        CuT.addMonths(1);
        assertEquals(CuT.getDayOfMonth(), copy.getDayOfMonth());
        assertEquals(CuT.getMonth()-1, copy.getMonth());
        assertEquals(CuT.getYear(), copy.getYear());
    }
}
