package com.mpagliaro98.mysubscriptions.ui.components;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.ZeroTimeCalendar;
import java.util.ArrayList;
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

    // The month and year the calendar is currently showing
    private int showingMonth;
    private int showingYear;

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
     * @param showingMonth the month currently being displayed by the calendar
     * @param showingYear the year currently being displayed by the calendar
     */
    public CalendarAdapter(Context context, ArrayList<Date> days, HashSet<Date> eventDays,
                           int showingMonth, int showingYear) {
        super(context, R.layout.component_subscriptioncalendar_day, days);
        this.eventDays = eventDays;
        this.inflater = LayoutInflater.from(context);
        this.showingMonth = showingMonth;
        this.showingYear = showingYear;
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
        ZeroTimeCalendar calendar = new ZeroTimeCalendar();
        calendar.setTimeToDate(date);
        int day = calendar.getDayOfMonth();
        int month = calendar.getMonth();
        int year = calendar.getYear();

        // Get today
        ZeroTimeCalendar today = new ZeroTimeCalendar();

        // Inflate the item if it does not exist yet
        if (view == null)
            view = inflater.inflate(R.layout.component_subscriptioncalendar_day, parent, false);

        // Put a minimum height of 35dp on each cell if the phone is in portrait orientation
        if (getContext().getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            float pxHeight = 35 * ((float) getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            view.setMinimumHeight((int)Math.ceil(pxHeight));
        }
        // Otherwise, use the screen height to estimate a proper min height in dp
        else {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            ((Activity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int screenHeightPx = displayMetrics.heightPixels;
            float screenHeightDp = screenHeightPx / ((float) getContext().getResources().getDisplayMetrics().densityDpi /
                    DisplayMetrics.DENSITY_DEFAULT);
            double gridSizeEstimate = (screenHeightDp * 0.66) - 90;
            int cellHeight = (int)Math.floor(gridSizeEstimate / 6);
            float pxHeight = cellHeight * ((float) getContext().getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
            view.setMinimumHeight((int)Math.ceil(pxHeight));
        }

        // If this day has an event, specify event image
        view.setBackgroundResource(0);
        if (eventDays != null) {
            for (Date eventDate : eventDays) {
                ZeroTimeCalendar eventDateCalendar = new ZeroTimeCalendar();
                eventDateCalendar.setTimeToDate(eventDate);
                if (eventDateCalendar.getDayOfMonth() == day &&
                        eventDateCalendar.getMonth() == month &&
                        eventDateCalendar.getYear() == year) {
                    // Mark this day for event
                    if (month != showingMonth || year != showingYear) {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.colorCalendarEventNotCurrent));
                    } else {
                        view.setBackgroundColor(getContext().getResources().getColor(R.color.colorCalendarEvent));
                    }
                    break;
                }
            }
        }

        // Clear the styling done on this grid space
        ((TextView)view).setTypeface(null, Typeface.NORMAL);
        ((TextView)view).setTextColor(Color.BLACK);

        // If this date is outside the current month, grey it out
        if (month != showingMonth || year != showingYear) {
            ((TextView)view).setTextColor(getContext().getResources().getColor(R.color.colorLightGreyBG));
        }
        // If this date is today, set it to the primary color and bold
        if (day == today.getDayOfMonth() && month == today.getMonth() &&
                year == today.getYear()) {
            ((TextView)view).setTypeface(null, Typeface.BOLD);
            if (month != showingMonth || year != showingYear) {
                ((TextView) view).setTextColor(getContext().getResources().getColor(R.color.colorCalendarTodayNotCurrent));
            } else {
                ((TextView) view).setTextColor(getContext().getResources().getColor(R.color.colorCalendarToday));
            }
        }

        // Set the number of this date
        ((TextView)view).setText(String.valueOf(calendar.getDayOfMonth()));
        return view;
    }
}
