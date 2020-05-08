package com.mpagliaro98.mysubscriptions.ui.components;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.mpagliaro98.mysubscriptions.R;

/**
 * Component class for the calendar UI element used in the calendar tab.
 * This code is based on the CalendarView created by Ahmed Al-Amir (https://github.com/ahmed-alamir/CalendarView)
 */
public class SubscriptionCalendar extends LinearLayout {

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create the subscription calendar element.
     * @param context the current application context
     */
    public SubscriptionCalendar(Context context) {
        super(context);
    }

    /**
     * Create the subscription calendar element.
     * @param context the current application context
     * @param attrs a set of attributes
     */
    public SubscriptionCalendar(Context context, AttributeSet attrs) {
        super(context, attrs);
        initSubCalendar(context);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the elements of the calendar and provide it with logic it needs to
     * function properly.
     * @param context the current application context
     */
    private void initSubCalendar(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        inflater.inflate(R.layout.component_subscriptioncalendar, this);
    }
}
