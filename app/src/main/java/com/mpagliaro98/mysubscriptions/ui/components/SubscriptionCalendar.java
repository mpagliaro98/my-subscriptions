package com.mpagliaro98.mysubscriptions.ui.components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.ZeroTimeCalendar;
import com.mpagliaro98.mysubscriptions.ui.interfaces.CalendarEventHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

/**
 * Component class for the calendar UI element used in the calendar tab.
 * This code is adapted from the CalendarView created by Ahmed Al-Amir (https://github.com/ahmed-alamir/CalendarView)
 */
public class SubscriptionCalendar extends LinearLayout {

    private ImageView btnPrev;
    private ImageView btnNext;
    private TextView txtDate;
    private GridView grid;
    private static final int DAYS_COUNT = 42;
    private ZeroTimeCalendar currentDate = new ZeroTimeCalendar();
    private String dateFormat;
    private CalendarEventHandler calendarEventHandler = null;
    private HashSet<Date> events = null;

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
        initSubCalendar(context, attrs);
    }

    /**
     * Create the subscription calendar element.
     * @param context the current application context
     * @param attrs a set of attributes
     * @param defStyleAttr a style attribute integer
     */
    public SubscriptionCalendar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initSubCalendar(context, attrs);
    }

    /**
     * Assign an event handler to this calendar. The handler will be called when a date
     * is selected on the view.
     * @param calendarEventHandler an instance of an object inheriting CalendarEventHandler
     */
    public void setCalendarEventHandler(CalendarEventHandler calendarEventHandler) {
        this.calendarEventHandler = calendarEventHandler;
    }

    /**
     * Give the calendar a set of events to highlight, that it will be able to hold onto
     * between calendar updates.
     * @param events a set of dates that represent days that should be highlighted in
     *               the calendar
     */
    public void setEvents(HashSet<Date> events) {
        this.events = events;
    }

    /**
     * Clear the set of events.
     */
    public void clearEvents() {
        this.events = null;
    }

    /**
     * Cause the calendar to update its appearance and data based on the current state.
     */
    public void updateCalendar() {
        // Initialize a list of dates for every cell in the calendar
        ArrayList<Date> cells = new ArrayList<>();
        ZeroTimeCalendar calendar = currentDate.copyCalendar();

        // Determine the cell for current month's beginning
        calendar.setTime(calendar.getYear(), calendar.getMonth(), 1);
        int monthBeginningCell = calendar.getDayOfWeek() - 1;

        // Move calendar backwards to the beginning of the week
        calendar.addDays(-monthBeginningCell);

        // Fill the cells with the dates they will contain
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getCurrentDate());
            calendar.addDays(1);
        }

        // Update the grid to display their proper views
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events,
                currentDate.getMonth(), currentDate.getYear()));

        // Update the title to the current month and year
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        txtDate.setText(sdf.format(currentDate.getCurrentDate()));
    }

    /**
     * Get the date currently being used by the calendar to represent the displayed
     * month. The month and year components of this date are what determines the month
     * that the calendar shows.
     * @return a date object representing what month the calendar is currently showing
     */
    public Date getDisplayedDate() {
        return currentDate.getCurrentDate();
    }

    /**
     * Given a date object, set the calendar to display the month and year of that given
     * date.
     * @param date a date object representing which month and year the calendar should be showing
     */
    public void setCalendarToMonth(Date date) {
        currentDate.setTimeToDate(date);
        updateCalendar();
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initialize the elements of the calendar and provide it with logic it needs to
     * function properly.
     * @param context the current application context
     */
    private void initSubCalendar(Context context, AttributeSet attrs) {
        // Inflate the view for the calendar
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        assert inflater != null;
        inflater.inflate(R.layout.component_subscriptioncalendar, this);

        // Initialize the data format, UI elements, and handlers
        loadDateFormat(context, attrs);
        assignUiElements();
        assignClickHandlers();

        // Update all the elements on the calendar and ready it to be viewable
        updateCalendar();
    }

    /**
     * Load in the date format from a previously defined style, or if none exists, use the
     * default format.
     * @param context the current application context
     * @param attrs the attribute set containing the date format
     */
    private void loadDateFormat(Context context, AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SubscriptionCalendar);
        try {
            // Try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.SubscriptionCalendar_dateFormat);
            if (dateFormat == null)
                dateFormat = context.getResources().getString(R.string.calendar_date_format);
        } finally {
            ta.recycle();
        }
    }

    /**
     * Save each important UI element to an instance variable here for easy access later.
     */
    private void assignUiElements() {
        btnPrev = findViewById(R.id.calendar_prev_button);
        btnNext = findViewById(R.id.calendar_next_button);
        txtDate = findViewById(R.id.calendar_date_display);
        grid = findViewById(R.id.calendar_grid);
    }

    /**
     * Assign click handlers to the previous and next buttons at the top, as well as to
     * every cell in the calendar.
     */
    private void assignClickHandlers() {
        // If the next button is pressed, go forward by one month and refresh the UI
        btnNext.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.addMonths(1);
                updateCalendar();
            }
        });

        // If the previous button is pressed, go back by one month and refresh the UI
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.addMonths(-1);
                updateCalendar();
            }
        });

        // If a day is pressed, run the given event handler if one exists
        grid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> view, View cell, int position, long id) {
                if (calendarEventHandler != null)
                    calendarEventHandler.onDayPress((Date)view.getItemAtPosition(position));
            }
        });
    }
}
