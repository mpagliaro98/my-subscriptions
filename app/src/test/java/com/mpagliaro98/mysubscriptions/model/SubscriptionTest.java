package com.mpagliaro98.mysubscriptions.model;

import android.content.res.Resources;
import com.mpagliaro98.mysubscriptions.R;
import org.junit.Before;
import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the Subscription class.
 */
public class SubscriptionTest {

    // The component under test
    private Subscription CuT;

    private ZeroTimeCalendar zeroTimeCalendar;

    /**
     * Run before each test, set the calendar to have zero time.
     */
    @Before
    public void setup() {
        zeroTimeCalendar = new ZeroTimeCalendar();
    }

    /**
     * Test creating a subscription and check that all fields are properly populated.
     */
    @Test
    public void test_create_subscription() {
        Date startDate = mock(Date.class);
        Category category = mock(Category.class);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, zeroTimeCalendar);
        assertEquals(0, CuT.getId());
        assertEquals("test", CuT.getName());
        assertEquals(4.33, CuT.getCost(), 0.01);
        assertEquals(startDate, CuT.getStartDate());
        assertEquals("test note", CuT.getNote());
        assertEquals(6, CuT.getRechargeFrequency());
        assertEquals(category, CuT.getCategory());
        assertEquals(7, CuT.getNotifDays());
    }

    /**
     * Test generation of the next payment date.
     */
    @Test
    public void test_next_payment_date() {
        zeroTimeCalendar.setTime(2021, 3, 5);
        Date startDate = zeroTimeCalendar.getCurrentDate();
        Category category = mock(Category.class);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, zeroTimeCalendar);
        assertEquals(6, CuT.getRechargeFrequency());

        // Start date is today, so next payment date stays today
        assertEquals(startDate, CuT.getNextPaymentDate());

        // Start date yesterday, so advance next payment date to six months in the future
        zeroTimeCalendar.setTime(2021, 3, 6);
        CuT.regenerateSubInfo(zeroTimeCalendar);
        zeroTimeCalendar.setTime(2021, 9, 5);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentDate());

        // Test future start date
        zeroTimeCalendar.setTime(2021, 5, 5);
        startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2021, 3, 5);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                12, category, 7, zeroTimeCalendar);
        assertEquals(12, CuT.getRechargeFrequency());
        assertEquals(startDate, CuT.getNextPaymentDate());

        // Advance by one year
        zeroTimeCalendar.setTime(2021, 5, 6);
        CuT.regenerateSubInfo(zeroTimeCalendar);
        zeroTimeCalendar.setTime(2022, 5, 5);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentDate());

        // Test incrementing by one month several times
        zeroTimeCalendar.setTime(2021, 5, 5);
        startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2024, 3, 20);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                1, category, 7, zeroTimeCalendar);
        assertEquals(1, CuT.getRechargeFrequency());
        zeroTimeCalendar.setTime(2024, 4, 5);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentDate());
    }

    /**
     * Test generation of the next payment list.
     */
    @Test
    public void test_next_payment_list() {
        zeroTimeCalendar.setTime(2021, 3, 5);
        Date startDate = zeroTimeCalendar.getCurrentDate();
        Category category = mock(Category.class);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, zeroTimeCalendar.copyCalendar());
        assertEquals(6, CuT.getRechargeFrequency());

        // Start date is today, so first next payment date stays today
        assertEquals(startDate, CuT.getNextPaymentList().get(0));
        zeroTimeCalendar.addMonths(6);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(1));
        zeroTimeCalendar.addMonths(6);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(2));
        zeroTimeCalendar.addMonths(6);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(3));

        // Start date yesterday, so advance next payment date to six months in the future
        zeroTimeCalendar.setTime(2021, 3, 6);
        CuT.regenerateSubInfo(zeroTimeCalendar);
        zeroTimeCalendar.setTime(2021, 9, 5);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(0));
        zeroTimeCalendar.addMonths(6);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(1));
        zeroTimeCalendar.addMonths(6);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(2));
        zeroTimeCalendar.addMonths(6);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(3));

        // Test future start date
        zeroTimeCalendar.setTime(2021, 5, 5);
        startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2021, 3, 5);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                12, category, 7, zeroTimeCalendar.copyCalendar());
        assertEquals(12, CuT.getRechargeFrequency());
        assertEquals(startDate, CuT.getNextPaymentList().get(0));
        zeroTimeCalendar.setTime(2021, 5, 5);
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(1));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(2));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(3));

        // Test incrementing by one month several times
        zeroTimeCalendar.setTime(2021, 5, 5);
        startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2024, 3, 20);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                1, category, 7, zeroTimeCalendar.copyCalendar());
        assertEquals(1, CuT.getRechargeFrequency());
        zeroTimeCalendar.setTime(2024, 4, 5);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(0));
        zeroTimeCalendar.addMonths(1);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(1));
        zeroTimeCalendar.addMonths(1);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(2));
        zeroTimeCalendar.addMonths(1);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(3));

        // Test the list holds 5 years of data
        zeroTimeCalendar.setTime(2021, 5, 5);
        startDate = zeroTimeCalendar.getCurrentDate();
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                12, category, 7, zeroTimeCalendar.copyCalendar());
        assertEquals(12, CuT.getRechargeFrequency());
        assertEquals(6, CuT.getNextPaymentList().size());
        assertEquals(startDate, CuT.getNextPaymentList().get(0));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(1));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(2));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(3));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(4));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(5));

        // Regenerate the last at a later date and ensure it still holds 5 years of data
        zeroTimeCalendar.setTime(2022, 6, 5);
        CuT.regenerateSubInfo(zeroTimeCalendar.copyCalendar());
        assertEquals(6, CuT.getNextPaymentList().size());
        zeroTimeCalendar.setTime(2023, 5, 5);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(0));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(1));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(2));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(3));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(4));
        zeroTimeCalendar.addMonths(12);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextPaymentList().get(5));
    }

    /**
     * Test generation of the next notification date.
     */
    @Test
    public void test_next_notification_date() {
        // Test null notification date
        zeroTimeCalendar.setTime(2021, 3, 5);
        Date startDate = zeroTimeCalendar.getCurrentDate();
        Category category = mock(Category.class);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, -1, zeroTimeCalendar);
        assertEquals(-1, CuT.getNotifDays());
        assertNull(CuT.getNextNotifDate());

        // Test notification a week before next payment, both in future
        zeroTimeCalendar.setTime(2021, 1, 20);
        startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2021, 3, 20);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, zeroTimeCalendar);
        assertEquals(7, CuT.getNotifDays());
        zeroTimeCalendar.setTime(2021, 7, 13);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextNotifDate());

        // Test notification date in the past, but payment date in the future
        zeroTimeCalendar.setTime(2021, 1, 20);
        startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2021, 4, 19);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                3, category, 2, zeroTimeCalendar);
        assertEquals(2, CuT.getNotifDays());
        zeroTimeCalendar.setTime(2021, 4, 18);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextNotifDate());

        // Test both the day of
        zeroTimeCalendar.setTime(2021, 1, 20);
        startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2021, 3, 20);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                2, category, 0, zeroTimeCalendar);
        assertEquals(0, CuT.getNotifDays());
        zeroTimeCalendar.setTime(2021, 3, 20);
        assertEquals(zeroTimeCalendar.getCurrentDate(), CuT.getNextNotifDate());
    }

    /**
     * Test formatting the cost and dates as strings.
     */
    @Test
    public void test_formatted_strings() {
        zeroTimeCalendar.setTime(2021, 3, 20);
        Date startDate = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTime(2021, 3, 21);
        Category category = mock(Category.class);
        Resources resources = mock(Resources.class);
        when(resources.getString(R.string.cost_format)).thenReturn("$%.2f");
        when(resources.getString(R.string.date_format)).thenReturn("MM/dd/yyyy");
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, zeroTimeCalendar);
        assertEquals("$4.33", CuT.getCostString(resources));
        assertEquals(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(startDate),
                CuT.getStartDateString(resources));
        zeroTimeCalendar.setTime(2021, 9, 20);
        assertEquals(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(zeroTimeCalendar.getCurrentDate()),
                CuT.getNextPaymentDateString(resources));

        // Test odd cost formatting
        CuT = new Subscription(0, "test", 4, startDate, "test note",
                6, category, 7, zeroTimeCalendar);
        assertEquals("$4.00", CuT.getCostString(resources));
        CuT = new Subscription(0, "test", 4.0213516, startDate, "test note",
                6, category, 7, zeroTimeCalendar);
        assertEquals("$4.02", CuT.getCostString(resources));
        CuT = new Subscription(0, "test", 4.039, startDate, "test note",
                6, category, 7, zeroTimeCalendar);
        assertEquals("$4.04", CuT.getCostString(resources));
    }
}
