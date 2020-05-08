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
import com.mpagliaro98.mysubscriptions.ui.interfaces.CalendarEventHandler;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    private Calendar currentDate = Calendar.getInstance();
    private static final String DATE_FORMAT = "MMM yyyy";
    private String dateFormat;
    private CalendarEventHandler calendarEventHandler = null;

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
     * Cause the calendar to update its appearance and data based on the current state.
     */
    public void updateCalendar() {
        updateCalendar(null);
    }

    /**
     * Cause the calendar to update its appearance and data based on the current state.
     * @param events a set of dates that represent days that should be highlighted in
     *               the calendar
     */
    public void updateCalendar(HashSet<Date> events) {
        // Initialize a list of dates for every cell in the calendar
        ArrayList<Date> cells = new ArrayList<>();
        Calendar calendar = (Calendar)currentDate.clone();

        // Determine the cell for current month's beginning
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        int monthBeginningCell = calendar.get(Calendar.DAY_OF_WEEK) - 1;

        // Move calendar backwards to the beginning of the week
        calendar.add(Calendar.DAY_OF_MONTH, -monthBeginningCell);

        // Fill the cells with the dates they will contain
        while (cells.size() < DAYS_COUNT) {
            cells.add(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        // Update the grid to display their proper views
        grid.setAdapter(new CalendarAdapter(getContext(), cells, events));

        // Update the title to the current month and year
        SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.US);
        txtDate.setText(sdf.format(currentDate.getTime()));
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
        loadDateFormat(attrs);
        assignUiElements();
        assignClickHandlers();

        // Update all the elements on the calendar and ready it to be viewable
        updateCalendar();
    }

    /**
     * Load in the date format from a previously defined style, or if none exists, use the
     * default format.
     * @param attrs the attribute set containing the date format
     */
    private void loadDateFormat(AttributeSet attrs) {
        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.SubscriptionCalendar);
        try {
            // Try to load provided date format, and fallback to default otherwise
            dateFormat = ta.getString(R.styleable.SubscriptionCalendar_dateFormat);
            if (dateFormat == null)
                dateFormat = DATE_FORMAT;
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
                currentDate.add(Calendar.MONTH, 1);
                updateCalendar();
            }
        });

        // If the previous button is pressed, go back by one month and refresh the UI
        btnPrev.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                currentDate.add(Calendar.MONTH, -1);
                updateCalendar();
            }
        });

        // If a day is pressed, run the given event handler if one exists
        grid.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> view, View cell, int position, long id) {
                if (calendarEventHandler == null)
                    return false;
                calendarEventHandler.onDayLongPress((Date)view.getItemAtPosition(position));
                return true;
            }
        });
    }
}
