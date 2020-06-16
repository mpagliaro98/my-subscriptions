package com.mpagliaro98.mysubscriptions.model;

import org.junit.Before;
import org.junit.Test;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the AnalyticsManager class.
 */
public class AnalyticsManagerTest {

    // The component under test
    private AnalyticsManager CuT;

    // Each analytics manager will need a mock model to get data from
    private SharedViewModel model;
    // A set of categories used to test breakdowns
    private Category catVideoStreaming;
    private Category catAudioStreaming;
    private Category catGaming;
    private Category catShopping;

    /**
     * Setup to do before each test. This creates a mock model class (with no replaced
     * methods) and creates the four fully-mocked category objects.
     */
    @Before
    public void setup() {
        model = mock(SharedViewModel.class);
        catVideoStreaming = mock(Category.class);
        when(catVideoStreaming.getName()).thenReturn("Video Streaming");
        when(catVideoStreaming.getColor()).thenReturn(0);
        catAudioStreaming = mock(Category.class);
        when(catAudioStreaming.getName()).thenReturn("Audio Streaming");
        when(catAudioStreaming.getColor()).thenReturn(1);
        catGaming = mock(Category.class);
        when(catGaming.getName()).thenReturn("Gaming");
        when(catGaming.getColor()).thenReturn(2);
        catShopping = mock(Category.class);
        when(catShopping.getName()).thenReturn("Online Shopping");
        when(catShopping.getColor()).thenReturn(3);
    }

    /**
     * Tests the analytic values when there are no subscriptions.
     */
    @Test
    public void test_analytics_1() {
        when(model.getFullSubscriptionList()).thenReturn(new ArrayList<Subscription>());
        CuT = new AnalyticsManager(model);

        assertEquals(0, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(0, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(0, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(0, CuT.getTotalDueYearly(), 0.01);
        assertEquals(0, CuT.getCostMostExpensive(), 0.01);
        assertEquals("", CuT.getNameMostExpensive());
        assertEquals(0, CuT.getMostCommonRecharge());
    }

    /**
     * Tests having multiple subscriptions in the same month, followed by none due in the
     * next month.
     */
    @Test
    public void test_analytics_2() {
        ZeroTimeCalendar baseZTC = new ZeroTimeCalendar();
        baseZTC.setTime(2020, 3, 1);

        Subscription sub1 = mock(Subscription.class);
        when(sub1.getName()).thenReturn("sub1");
        when(sub1.getCategory()).thenReturn(catVideoStreaming);
        when(sub1.getCost()).thenReturn(3.99);
        when(sub1.getRechargeFrequency()).thenReturn(2);
        when(sub1.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub1.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub2 = mock(Subscription.class);
        when(sub2.getName()).thenReturn("sub2");
        when(sub2.getCategory()).thenReturn(catVideoStreaming);
        when(sub2.getCost()).thenReturn(5.99);
        when(sub2.getRechargeFrequency()).thenReturn(3);
        baseZTC.setTime(2020, 3, 2);
        when(sub2.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub2.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        List<Subscription> list = new ArrayList<>();
        list.add(sub1);
        list.add(sub2);
        when(model.getFullSubscriptionList()).thenReturn(list);
        baseZTC.setTime(2020, 3, 1);
        CuT = new AnalyticsManager(model, baseZTC);

        assertEquals(9.98, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(9.98, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(0, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(47.9, CuT.getTotalDueYearly(), 0.01);
        assertEquals(23.96, CuT.getCostMostExpensive(), 0.01);
        assertEquals("sub2", CuT.getNameMostExpensive());
        assertEquals(0, CuT.getMostCommonRecharge());
    }

    /**
     * Tests all subscriptions having the same recharge frequency, so the analytics manager
     * should choose that frequency as being the most common.
     */
    @Test
    public void test_analytics_3() {
        ZeroTimeCalendar baseZTC = new ZeroTimeCalendar();
        baseZTC.setTime(2020, 3, 1);

        Subscription sub1 = mock(Subscription.class);
        when(sub1.getName()).thenReturn("sub1");
        when(sub1.getCategory()).thenReturn(catVideoStreaming);
        when(sub1.getCost()).thenReturn(3.99);
        when(sub1.getRechargeFrequency()).thenReturn(2);
        when(sub1.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub1.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub2 = mock(Subscription.class);
        when(sub2.getName()).thenReturn("sub2");
        when(sub2.getCategory()).thenReturn(catVideoStreaming);
        when(sub2.getCost()).thenReturn(5.99);
        when(sub2.getRechargeFrequency()).thenReturn(2);
        baseZTC.setTime(2020, 3, 2);
        when(sub2.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub2.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub3 = mock(Subscription.class);
        when(sub3.getName()).thenReturn("sub3");
        when(sub3.getCategory()).thenReturn(catVideoStreaming);
        when(sub3.getCost()).thenReturn(7.99);
        when(sub3.getRechargeFrequency()).thenReturn(2);
        baseZTC.setTime(2020, 3, 3);
        when(sub3.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub3.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        List<Subscription> list = new ArrayList<>();
        list.add(sub1);
        list.add(sub2);
        list.add(sub3);
        when(model.getFullSubscriptionList()).thenReturn(list);
        baseZTC.setTime(2020, 3, 1);
        CuT = new AnalyticsManager(model, baseZTC);

        assertEquals(17.97, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(17.97, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(0, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(107.82, CuT.getTotalDueYearly(), 0.01);
        assertEquals(47.94, CuT.getCostMostExpensive(), 0.01);
        assertEquals("sub3", CuT.getNameMostExpensive());
        assertEquals(2, CuT.getMostCommonRecharge());
    }

    /**
     * Tests each subscription having a different recharge frequency, so the analytics manager
     * shouldn't choose any of them as being the most common. This also tests some subscription
     * dates in the current month being in the past, so the rest of month total should be
     * lower than the month total.
     */
    @Test
    public void test_analytics_4() {
        ZeroTimeCalendar baseZTC = new ZeroTimeCalendar();
        baseZTC.setTime(2020, 3, 1);

        Subscription sub1 = mock(Subscription.class);
        when(sub1.getName()).thenReturn("sub1");
        when(sub1.getCategory()).thenReturn(catVideoStreaming);
        when(sub1.getCost()).thenReturn(3.99);
        when(sub1.getRechargeFrequency()).thenReturn(2);
        when(sub1.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        baseZTC.addMonths(2);
        when(sub1.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub2 = mock(Subscription.class);
        when(sub2.getName()).thenReturn("sub2");
        when(sub2.getCategory()).thenReturn(catVideoStreaming);
        when(sub2.getCost()).thenReturn(5.99);
        when(sub2.getRechargeFrequency()).thenReturn(3);
        baseZTC.setTime(2020, 3, 2);
        when(sub2.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        baseZTC.addMonths(3);
        when(sub2.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub3 = mock(Subscription.class);
        when(sub3.getName()).thenReturn("sub3");
        when(sub3.getCategory()).thenReturn(catVideoStreaming);
        when(sub3.getCost()).thenReturn(7.99);
        when(sub3.getRechargeFrequency()).thenReturn(4);
        baseZTC.setTime(2020, 3, 3);
        when(sub3.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub3.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        List<Subscription> list = new ArrayList<>();
        list.add(sub1);
        list.add(sub2);
        list.add(sub3);
        when(model.getFullSubscriptionList()).thenReturn(list);
        baseZTC.setTime(2020, 3, 3);
        CuT = new AnalyticsManager(model, baseZTC);

        assertEquals(17.97, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(7.99, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(0, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(71.87, CuT.getTotalDueYearly(), 0.01);
        assertEquals(23.97, CuT.getCostMostExpensive(), 0.01);
        assertEquals("sub3", CuT.getNameMostExpensive());
        assertEquals(0, CuT.getMostCommonRecharge());
    }

    /**
     * Tests the rest of month total when today is before both subscriptions, then when one
     * subscription has passed and one is in the future, and then when both are in the past.
     * This also does these tests while some subscriptions are due on today's date.
     */
    @Test
    public void test_analytics_5() {
        ZeroTimeCalendar baseZTC = new ZeroTimeCalendar();
        baseZTC.setTime(2020, 3, 1);

        Subscription sub1 = mock(Subscription.class);
        when(sub1.getName()).thenReturn("sub1");
        when(sub1.getCategory()).thenReturn(catVideoStreaming);
        when(sub1.getCost()).thenReturn(3.99);
        when(sub1.getRechargeFrequency()).thenReturn(1);
        when(sub1.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub1.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub2 = mock(Subscription.class);
        when(sub2.getName()).thenReturn("sub2");
        when(sub2.getCategory()).thenReturn(catVideoStreaming);
        when(sub2.getCost()).thenReturn(5.99);
        when(sub2.getRechargeFrequency()).thenReturn(1);
        baseZTC.setTime(2020, 3, 2);
        when(sub2.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub2.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        List<Subscription> list = new ArrayList<>();
        list.add(sub1);
        list.add(sub2);
        when(model.getFullSubscriptionList()).thenReturn(list);
        baseZTC.setTime(2020, 3, 1);
        CuT = new AnalyticsManager(model, baseZTC);

        assertEquals(9.98, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(9.98, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(9.98, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(119.76, CuT.getTotalDueYearly(), 0.01);
        assertEquals(71.88, CuT.getCostMostExpensive(), 0.01);
        assertEquals("sub2", CuT.getNameMostExpensive());
        assertEquals(1, CuT.getMostCommonRecharge());

        baseZTC.setTime(2020, 4, 1);
        when(sub1.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());
        baseZTC.setTime(2020, 3, 2);
        CuT = new AnalyticsManager(model, baseZTC);

        assertEquals(9.98, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(5.99, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(9.98, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(119.76, CuT.getTotalDueYearly(), 0.01);
        assertEquals(71.88, CuT.getCostMostExpensive(), 0.01);
        assertEquals("sub2", CuT.getNameMostExpensive());
        assertEquals(1, CuT.getMostCommonRecharge());

        baseZTC.setTime(2020, 4, 2);
        when(sub2.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());
        baseZTC.setTime(2020, 3, 3);
        CuT = new AnalyticsManager(model, baseZTC);

        assertEquals(9.98, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(0, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(9.98, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(119.76, CuT.getTotalDueYearly(), 0.01);
        assertEquals(71.88, CuT.getCostMostExpensive(), 0.01);
        assertEquals("sub2", CuT.getNameMostExpensive());
        assertEquals(1, CuT.getMostCommonRecharge());
    }

    /**
     * Tests the most expensive yearly analytic, having one subscription being expensive but
     * charged only once per year, then having another subscription being cheap but charged
     * monthly, and the cheap subscription should be more expensive yearly.
     */
    @Test
    public void test_analytics_6() {
        ZeroTimeCalendar baseZTC = new ZeroTimeCalendar();
        baseZTC.setTime(2020, 3, 1);

        Subscription sub1 = mock(Subscription.class);
        when(sub1.getName()).thenReturn("sub1");
        when(sub1.getCategory()).thenReturn(catVideoStreaming);
        when(sub1.getCost()).thenReturn(1.0);
        when(sub1.getRechargeFrequency()).thenReturn(1);
        when(sub1.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub1.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub2 = mock(Subscription.class);
        when(sub2.getName()).thenReturn("sub2");
        when(sub2.getCategory()).thenReturn(catVideoStreaming);
        when(sub2.getCost()).thenReturn(11.99);
        when(sub2.getRechargeFrequency()).thenReturn(12);
        baseZTC.setTime(2020, 3, 2);
        when(sub2.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub2.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        List<Subscription> list = new ArrayList<>();
        list.add(sub1);
        list.add(sub2);
        when(model.getFullSubscriptionList()).thenReturn(list);
        baseZTC.setTime(2020, 3, 1);
        CuT = new AnalyticsManager(model, baseZTC);

        assertEquals(12.99, CuT.getTotalDueThisMonth(), 0.01);
        assertEquals(12.99, CuT.getRestDueThisMonth(), 0.01);
        assertEquals(1.0, CuT.getTotalDueNextMonth(), 0.01);
        assertEquals(23.99, CuT.getTotalDueYearly(), 0.01);
        assertEquals(12.0, CuT.getCostMostExpensive(), 0.01);
        assertEquals("sub1", CuT.getNameMostExpensive());
        assertEquals(0, CuT.getMostCommonRecharge());
    }

    /**
     * Tests breakdowns. The one month breakdown should have one category only, the 2 month
     * one should have still one category but a higher total, then each subsequent
     * larger breakdown should include additional categories and show the totals increasing.
     */
    @Test
    public void test_analytics_7() {
        ZeroTimeCalendar baseZTC = new ZeroTimeCalendar();
        baseZTC.setTime(2020, 3, 1);

        Subscription sub1 = mock(Subscription.class);
        when(sub1.getName()).thenReturn("sub1");
        when(sub1.getCategory()).thenReturn(catVideoStreaming);
        when(sub1.getCost()).thenReturn(1.0);
        when(sub1.getRechargeFrequency()).thenReturn(2);
        when(sub1.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub1.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub2 = mock(Subscription.class);
        when(sub2.getName()).thenReturn("sub2");
        when(sub2.getCategory()).thenReturn(catVideoStreaming);
        when(sub2.getCost()).thenReturn(2.0);
        when(sub2.getRechargeFrequency()).thenReturn(1);
        baseZTC.setTime(2020, 3, 2);
        when(sub2.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub2.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub3 = mock(Subscription.class);
        when(sub3.getName()).thenReturn("sub3");
        when(sub3.getCategory()).thenReturn(catAudioStreaming);
        when(sub3.getCost()).thenReturn(3.0);
        when(sub3.getRechargeFrequency()).thenReturn(3);
        baseZTC.setTime(2020, 5, 5);
        when(sub3.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub3.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub4 = mock(Subscription.class);
        when(sub4.getName()).thenReturn("sub4");
        when(sub4.getCategory()).thenReturn(catGaming);
        when(sub4.getCost()).thenReturn(5.0);
        when(sub4.getRechargeFrequency()).thenReturn(6);
        baseZTC.setTime(2020, 8, 13);
        when(sub4.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub4.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        Subscription sub5 = mock(Subscription.class);
        when(sub5.getName()).thenReturn("sub5");
        when(sub5.getCategory()).thenReturn(catShopping);
        when(sub5.getCost()).thenReturn(9.0);
        when(sub5.getRechargeFrequency()).thenReturn(1);
        baseZTC.setTime(2021, 2, 7);
        when(sub5.getStartDate()).thenReturn(baseZTC.getCurrentDate());
        when(sub5.getNextPaymentDate()).thenReturn(baseZTC.getCurrentDate());

        List<Subscription> list = new ArrayList<>();
        list.add(sub1);
        list.add(sub2);
        list.add(sub3);
        list.add(sub4);
        list.add(sub5);
        when(model.getFullSubscriptionList()).thenReturn(list);
        baseZTC.setTime(2020, 3, 1);
        CuT = new AnalyticsManager(model, baseZTC);

        CuT.createMonthlyBreakdown(1);
        List<Map.Entry<Category, Double>> breakdown = CuT.getBreakdownList();
        assertEquals(1, breakdown.size());
        assertEquals(catVideoStreaming, breakdown.get(0).getKey());
        assertEquals(3.0, breakdown.get(0).getValue(), 0.01);

        CuT.createMonthlyBreakdown(2);
        breakdown = CuT.getBreakdownList();
        assertEquals(1, breakdown.size());
        assertEquals(catVideoStreaming, breakdown.get(0).getKey());
        assertEquals(5.0, breakdown.get(0).getValue(), 0.01);

        CuT.createMonthlyBreakdown(3);
        breakdown = CuT.getBreakdownList();
        assertEquals(2, breakdown.size());
        assertEquals(catVideoStreaming, breakdown.get(0).getKey());
        assertEquals(8.0, breakdown.get(0).getValue(), 0.01);
        assertEquals(catAudioStreaming, breakdown.get(1).getKey());
        assertEquals(3.0, breakdown.get(1).getValue(), 0.01);

        CuT.createMonthlyBreakdown(6);
        breakdown = CuT.getBreakdownList();
        assertEquals(3, breakdown.size());
        assertEquals(catVideoStreaming, breakdown.get(0).getKey());
        assertEquals(15.0, breakdown.get(0).getValue(), 0.01);
        assertEquals(catAudioStreaming, breakdown.get(1).getKey());
        assertEquals(6.0, breakdown.get(1).getValue(), 0.01);
        assertEquals(catGaming, breakdown.get(2).getKey());
        assertEquals(5.0, breakdown.get(2).getValue(), 0.01);

        CuT.createMonthlyBreakdown(12);
        breakdown = CuT.getBreakdownList();
        assertEquals(4, breakdown.size());
        assertEquals(catVideoStreaming, breakdown.get(0).getKey());
        assertEquals(30.0, breakdown.get(0).getValue(), 0.01);
        assertEquals(catAudioStreaming, breakdown.get(1).getKey());
        assertEquals(12.0, breakdown.get(1).getValue(), 0.01);
        assertEquals(catGaming, breakdown.get(2).getKey());
        assertEquals(10.0, breakdown.get(2).getValue(), 0.01);
        assertEquals(catShopping, breakdown.get(3).getKey());
        assertEquals(9.0, breakdown.get(3).getValue(), 0.01);
    }
}
