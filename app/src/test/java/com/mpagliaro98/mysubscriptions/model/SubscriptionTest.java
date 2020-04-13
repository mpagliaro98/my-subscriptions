package com.mpagliaro98.mysubscriptions.model;

import android.content.res.Resources;
import com.mpagliaro98.mysubscriptions.R;
import org.junit.Before;
import org.junit.Test;
import java.text.SimpleDateFormat;
import java.util.Calendar;
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

    private Calendar calendar = Calendar.getInstance();

    /**
     * Run before each test, set the calendar to have zero time.
     */
    @Before
    public void setup() {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * Test creating a subscription and check that all fields are properly populated.
     */
    @Test
    public void test_create_subscription() {
        Date startDate = mock(Date.class);
        Category category = mock(Category.class);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, calendar);
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
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Date startDate = calendar.getTime();
        Category category = mock(Category.class);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, calendar);
        assertEquals(6, CuT.getRechargeFrequency());

        // Start date is today, so next payment date stays today
        assertEquals(startDate, CuT.getNextPaymentDate());

        // Start date yesterday, so advance next payment date to six months in the future
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 6);
        CuT.regenerateSubInfo(calendar);
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        calendar.set(Calendar.MONTH, 9);
        assertEquals(calendar.getTime(), CuT.getNextPaymentDate());

        // Test future start date
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                12, category, 7, calendar);
        assertEquals(12, CuT.getRechargeFrequency());
        assertEquals(startDate, CuT.getNextPaymentDate());

        // Advance by one year
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 6);
        CuT.regenerateSubInfo(calendar);
        calendar.set(Calendar.YEAR, 2022);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        assertEquals(calendar.getTime(), CuT.getNextPaymentDate());

        // Test incrementing by one month several times
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 5);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, 2024);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                1, category, 7, calendar);
        assertEquals(1, CuT.getRechargeFrequency());
        calendar.set(Calendar.YEAR, 2024);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        assertEquals(calendar.getTime(), CuT.getNextPaymentDate());
    }

    /**
     * Test generation of the next notification date.
     */
    @Test
    public void test_next_notification_date() {
        // Test null notification date
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Date startDate = calendar.getTime();
        Category category = mock(Category.class);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, -1, calendar);
        assertEquals(-1, CuT.getNotifDays());
        assertNull(CuT.getNextNotifDate());

        // Test notification a week before next payment, both in future
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, calendar);
        assertEquals(7, CuT.getNotifDays());
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 7);
        calendar.set(Calendar.DAY_OF_MONTH, 13);
        assertEquals(calendar.getTime(), CuT.getNextNotifDate());

        // Test notification date in the past, but payment date in the future
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 19);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                3, category, 2, calendar);
        assertEquals(2, CuT.getNotifDays());
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 4);
        calendar.set(Calendar.DAY_OF_MONTH, 18);
        assertEquals(calendar.getTime(), CuT.getNextNotifDate());

        // Test both the day of
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                2, category, 0, calendar);
        assertEquals(0, CuT.getNotifDays());
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        assertEquals(calendar.getTime(), CuT.getNextNotifDate());
    }

    /**
     * Test formatting the cost and dates as strings.
     */
    @Test
    public void test_formatted_strings() {
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        Date startDate = calendar.getTime();
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 21);
        Category category = mock(Category.class);
        Resources resources = mock(Resources.class);
        when(resources.getString(R.string.cost_format)).thenReturn("$%.2f");
        when(resources.getString(R.string.date_format)).thenReturn("MM/dd/yyyy");
        CuT = new Subscription(0, "test", 4.33, startDate, "test note",
                6, category, 7, calendar);
        assertEquals("$4.33", CuT.getCostString(resources));
        assertEquals(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(startDate),
                CuT.getStartDateString(resources));
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 9);
        calendar.set(Calendar.DAY_OF_MONTH, 20);
        assertEquals(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(calendar.getTime()),
                CuT.getNextPaymentDateString(resources));

        // Test odd cost formatting
        CuT = new Subscription(0, "test", 4, startDate, "test note",
                6, category, 7, calendar);
        assertEquals("$4.00", CuT.getCostString(resources));
        CuT = new Subscription(0, "test", 4.0213516, startDate, "test note",
                6, category, 7, calendar);
        assertEquals("$4.02", CuT.getCostString(resources));
        CuT = new Subscription(0, "test", 4.039, startDate, "test note",
                6, category, 7, calendar);
        assertEquals("$4.04", CuT.getCostString(resources));
    }
}
