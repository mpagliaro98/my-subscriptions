package com.mpagliaro98.mysubscriptions.model;

import org.junit.Before;
import org.junit.Test;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the SharedViewModel class.
 */
public class SharedViewModelTest {

    // The component under test
    private SharedViewModel CuT;

    // Any objects needed by the component to be mocked
    private Subscription sub1, sub2, sub3;

    /**
     * Run before every test, initializes the component under test and the mock objects.
     */
    @Before
    public void setup() {
        CuT = new SharedViewModel();

        // Create categories necessary for sorting
        Category cat1 = mock(Category.class);
        Category cat2 = mock(Category.class);
        when(cat1.getName()).thenReturn("cat1");
        when(cat2.getName()).thenReturn("cat2");

        // Create the first subscription
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 2022);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 6);
        Date date1 = calendar.getTime();
        sub1 = mock(Subscription.class);
        when(sub1.getId()).thenReturn(0);
        when(sub1.getName()).thenReturn("test sub1");
        when(sub1.getCost()).thenReturn(3.45);
        when(sub1.getNextPaymentDate()).thenReturn(date1);
        when(sub1.getCategory()).thenReturn(cat1);

        // Create the second subscription
        calendar.set(Calendar.YEAR, 2022);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 6);
        Date date2 = calendar.getTime();
        sub2 = mock(Subscription.class);
        when(sub2.getId()).thenReturn(1);
        when(sub2.getName()).thenReturn("sub2 TEST");
        when(sub2.getCost()).thenReturn(3.44);
        when(sub2.getNextPaymentDate()).thenReturn(date2);
        when(sub2.getCategory()).thenReturn(cat1);

        // Create the third subscription
        calendar.set(Calendar.YEAR, 2021);
        calendar.set(Calendar.MONTH, 3);
        calendar.set(Calendar.DAY_OF_MONTH, 5);
        Date date3 = calendar.getTime();
        sub3 = mock(Subscription.class);
        when(sub3.getId()).thenReturn(2);
        when(sub3.getName()).thenReturn("don't filter sub3");
        when(sub3.getCost()).thenReturn(10.10101010101);
        when(sub3.getNextPaymentDate()).thenReturn(date3);
        when(sub3.getCategory()).thenReturn(cat2);
    }

    /**
     * Test adding a subscription to the model.
     */
    @Test
    public void test_add_subscription() {
        CuT.addSubscription(sub1);
        assertEquals(1, CuT.numSubscriptionsTotal());
        assertEquals(1, CuT.numSubscriptionsVisible());
        List<Subscription> subList = CuT.getFullSubscriptionList();
        assertEquals(sub1, subList.get(0));
    }

    /**
     * Test adding a subscription, then updating it.
     */
    @Test
    public void test_update_subscription() {
        CuT.addSubscription(sub1);
        CuT.updateSubscription(sub2, 0);
        List<Subscription> subList = CuT.getFullSubscriptionList();
        assertEquals(sub2, subList.get(0));
        assertEquals(1, sub2.getId());
    }

    /**
     * Test adding a subscription, then deleting it.
     */
    @Test
    public void test_delete_subscription() {
        CuT.addSubscription(sub1);
        assertEquals(1, CuT.numSubscriptionsTotal());
        assertEquals(1, CuT.numSubscriptionsVisible());
        CuT.deleteSubscription(0);
        assertEquals(0, CuT.numSubscriptionsTotal());
        assertEquals(0, CuT.numSubscriptionsVisible());
    }

    /**
     * Test adding several subscriptions, then deleting one in the middle.
     */
    @Test
    public void test_add_multiple_subscriptions() {
        CuT.addSubscription(sub1);
        assertEquals(1, CuT.numSubscriptionsTotal());
        assertEquals(1, CuT.numSubscriptionsVisible());
        CuT.addSubscription(sub2);
        assertEquals(2, CuT.numSubscriptionsTotal());
        assertEquals(2, CuT.numSubscriptionsVisible());
        CuT.addSubscription(sub3);
        assertEquals(3, CuT.numSubscriptionsTotal());
        assertEquals(3, CuT.numSubscriptionsVisible());
        CuT.deleteSubscription(1);
        assertEquals(2, CuT.numSubscriptionsTotal());
        assertEquals(2, CuT.numSubscriptionsVisible());
        List<Subscription> subList = CuT.getFullSubscriptionList();
        assertEquals(sub1, subList.get(0));
        assertEquals(sub3, subList.get(1));
    }

    /**
     * Test filtering the subscriptions by name.
     */
    @Test
    public void test_filter() {
        CuT.addSubscription(sub1);
        CuT.addSubscription(sub2);
        CuT.addSubscription(sub3);
        // Check the model is valid before filtering
        assertEquals(3, CuT.numSubscriptionsTotal());
        assertEquals(3, CuT.numSubscriptionsVisible());
        assertEquals(sub1, CuT.getSubscription(0));
        // Filter that changes nothing
        CuT.filterList("sub");
        assertEquals(3, CuT.numSubscriptionsVisible());
        // Filter off sub3
        CuT.filterList("test");
        assertEquals(2, CuT.numSubscriptionsVisible());
        assertEquals(3, CuT.numSubscriptionsTotal());
        // Filter with all caps
        CuT.filterList("TEST");
        assertEquals(2, CuT.numSubscriptionsVisible());
        assertEquals(3, CuT.numSubscriptionsTotal());
        // Filter down to one
        CuT.filterList("2");
        assertEquals(1, CuT.numSubscriptionsVisible());
        assertEquals(3, CuT.numSubscriptionsTotal());
        assertEquals(sub2, CuT.getSubscription(0));
        // Filter everything away
        CuT.filterList("4");
        assertEquals(0, CuT.numSubscriptionsVisible());
        assertEquals(3, CuT.numSubscriptionsTotal());
        // Filter some things back to ensure no data is lost
        CuT.filterList("TeSt");
        assertEquals(2, CuT.numSubscriptionsVisible());
        assertEquals(3, CuT.numSubscriptionsTotal());
        assertEquals(sub1, CuT.getSubscription(0));
        assertEquals(sub2, CuT.getSubscription(1));
    }

    /**
     * Test sorting the subscriptions by ID.
     */
    @Test
    public void test_sort_none() {
        CuT.addSubscription(sub1);
        CuT.addSubscription(sub2);
        CuT.addSubscription(sub3);
        CuT.sortList(new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                return o1.getId() < o2.getId() ? -1 : 1;
            }
        }, "");
        Subscription pos1 = CuT.getSubscription(0);
        Subscription pos2 = CuT.getSubscription(1);
        Subscription pos3 = CuT.getSubscription(2);
        assertEquals(sub1, pos1);
        assertEquals(sub2, pos2);
        assertEquals(sub3, pos3);
    }

    /**
     * Test sorting the subscriptions by name.
     */
    @Test
    public void test_sort_name() {
        CuT.addSubscription(sub1);
        CuT.addSubscription(sub2);
        CuT.addSubscription(sub3);
        CuT.sortList(new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        }, "");
        Subscription pos1 = CuT.getSubscription(0);
        Subscription pos2 = CuT.getSubscription(1);
        Subscription pos3 = CuT.getSubscription(2);
        assertEquals(sub3, pos1);
        assertEquals(sub2, pos2);
        assertEquals(sub1, pos3);
    }

    /**
     * Test sorting the subscriptions by cost.
     */
    @Test
    public void test_sort_cost() {
        CuT.addSubscription(sub1);
        CuT.addSubscription(sub2);
        CuT.addSubscription(sub3);
        CuT.sortList(new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                if (o1.getCost() == o2.getCost()) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                } else {
                    return o1.getCost() < o2.getCost() ? -1 : 1;
                }
            }
        }, "");
        Subscription pos1 = CuT.getSubscription(0);
        Subscription pos2 = CuT.getSubscription(1);
        Subscription pos3 = CuT.getSubscription(2);
        assertEquals(sub2, pos1);
        assertEquals(sub1, pos2);
        assertEquals(sub3, pos3);
    }

    /**
     * Test sorting the subscriptions by next payment date.
     */
    @Test
    public void test_sort_date() {
        CuT.addSubscription(sub1);
        CuT.addSubscription(sub2);
        CuT.addSubscription(sub3);
        CuT.sortList(new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                if (o1.getNextPaymentDate().equals(o2.getNextPaymentDate())) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                } else {
                    return o1.getNextPaymentDate().compareTo(o2.getNextPaymentDate());
                }
            }
        }, "");
        Subscription pos1 = CuT.getSubscription(0);
        Subscription pos2 = CuT.getSubscription(1);
        Subscription pos3 = CuT.getSubscription(2);
        assertEquals(sub3, pos1);
        assertEquals(sub2, pos2);
        assertEquals(sub1, pos3);
    }

    /**
     * Test sorting the subscriptions by category.
     */
    @Test
    public void test_sort_category() {
        CuT.addSubscription(sub1);
        CuT.addSubscription(sub2);
        CuT.addSubscription(sub3);
        CuT.sortList(new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                if (o1.getCategory().getName().equals(o2.getCategory().getName())) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                } else {
                    return o1.getCategory().getName().compareToIgnoreCase(o2.getCategory().getName());
                }
            }
        }, "");
        Subscription pos1 = CuT.getSubscription(0);
        Subscription pos2 = CuT.getSubscription(1);
        Subscription pos3 = CuT.getSubscription(2);
        assertEquals(sub2, pos1);
        assertEquals(sub1, pos2);
        assertEquals(sub3, pos3);
    }

    /**
     * Test sorting the subscriptions, then filtering them while still sorted.
     */
    @Test
    public void test_sort_and_filter() {
        CuT.addSubscription(sub1);
        CuT.addSubscription(sub2);
        CuT.addSubscription(sub3);
        CuT.sortList(new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        }, "");
        assertEquals(3, CuT.numSubscriptionsTotal());
        assertEquals(3, CuT.numSubscriptionsVisible());
        CuT.filterList("test");
        assertEquals(3, CuT.numSubscriptionsTotal());
        assertEquals(2, CuT.numSubscriptionsVisible());
        Subscription pos1 = CuT.getSubscription(0);
        Subscription pos2 = CuT.getSubscription(1);
        assertEquals(sub2, pos1);
        assertEquals(sub1, pos2);
        CuT.filterList("");
        assertEquals(3, CuT.numSubscriptionsVisible());
        pos1 = CuT.getSubscription(0);
        pos2 = CuT.getSubscription(1);
        Subscription pos3 = CuT.getSubscription(2);
        assertEquals(sub3, pos1);
        assertEquals(sub2, pos2);
        assertEquals(sub1, pos3);
        CuT.filterList("DON'T");
        assertEquals(1, CuT.numSubscriptionsVisible());
        pos1 = CuT.getSubscription(0);
        assertEquals(sub3, pos1);
        CuT.sortList(new Comparator<Subscription>() {
            @Override
            public int compare(Subscription o1, Subscription o2) {
                if (o1.getCost() == o2.getCost()) {
                    return o1.getName().compareToIgnoreCase(o2.getName());
                } else {
                    return o1.getCost() < o2.getCost() ? -1 : 1;
                }
            }
        }, "DON'T");
        assertEquals(1, CuT.numSubscriptionsVisible());
        pos1 = CuT.getSubscription(0);
        assertEquals(sub3, pos1);
        CuT.filterList("");
        assertEquals(3, CuT.numSubscriptionsVisible());
        pos1 = CuT.getSubscription(0);
        pos2 = CuT.getSubscription(1);
        pos3 = CuT.getSubscription(2);
        assertEquals(sub2, pos1);
        assertEquals(sub1, pos2);
        assertEquals(sub3, pos3);
    }
}
