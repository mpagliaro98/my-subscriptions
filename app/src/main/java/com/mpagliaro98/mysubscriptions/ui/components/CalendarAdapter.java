package com.mpagliaro98.mysubscriptions.ui.components;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.mpagliaro98.mysubscriptions.R;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

/**
 * Component class for the calendar UI element used in the calendar tab.
 * This code is adapted from the CalendarView created by Ahmed Al-Amir (https://github.com/ahmed-alamir/CalendarView)
 */
public class CalendarAdapter extends ArrayAdapter<Date> {

    // Set of days with events that should be highlighted
    private HashSet<Date> eventDays;

    // The view inflater
    private LayoutInflater inflater;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create this adapter and initialize the set of events and the inflater that will
     * be used later when the view of the given day is needed.
     * @param context the current application context
     * @param days a list of days that make up the current month (and parts of the previous
     *             and next months) that is visible in the calendar
     * @param eventDays a set of days that contain events and should be highlighted
     */
    public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays) {
        super(context, R.layout.component_subscriptioncalendar_day, days);
        this.eventDays = eventDays;
        inflater = LayoutInflater.from(context);
    }

    /**
     * Create and return the view of the given day on the calendar. This will also apply
     * styling to the day if it is outside the current month or has an event associated
     * with it.
     * @param position the position in the original array this day is at
     * @param view the subscription calendar day view that makes up this space on the
     *             calendar, in this case it's a TextView
     * @param parent the parent ViewGroup of this view
     * @return the finished version of the passed in view that should be displayed
     */
    @Override
    @NonNull
    public View getView(int position, View view, @NonNull ViewGroup parent) {
        // Get the date at this position
        Date date = getItem(position);
        assert date != null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);

        // Get today
        Calendar today = Calendar.getInstance();

        // Inflate the item if it does not exist yet
        if (view == null)
            view = inflater.inflate(R.layout.component_subscriptioncalendar_day, parent, false);

        // If this day has an event, specify event image
        view.setBackgroundResource(0);
        if (eventDays != null) {
            for (Date eventDate : eventDays) {
                Calendar eventDateCalendar = Calendar.getInstance();
                eventDateCalendar.setTime(eventDate);
                if (eventDateCalendar.get(Calendar.DAY_OF_MONTH) == day &&
                        eventDateCalendar.get(Calendar.MONTH) == month &&
                        eventDateCalendar.get(Calendar.YEAR) == year) {
                    // Mark this day for event
                    view.setBackgroundResource(R.drawable.reminder);
                    break;
                }
            }
        }

        // Clear the styling done on this grid space
        ((TextView)view).setTypeface(null, Typeface.NORMAL);
        ((TextView)view).setTextColor(Color.BLACK);

        // If this date is outside the current month, grey it out
        if (month != today.get(Calendar.MONTH) || year != today.get(Calendar.YEAR)) {
            ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.colorLightGreyBG));
        }
        // If this date is today, set it to the primary color and bold
        else if (day == today.get(Calendar.DAY_OF_MONTH)) {
            ((TextView)view).setTypeface(null, Typeface.BOLD);
            ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.colorPrimary));
        }

        // Set the number of this date
        ((TextView)view).setText(String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)));
        return view;
    }
}
