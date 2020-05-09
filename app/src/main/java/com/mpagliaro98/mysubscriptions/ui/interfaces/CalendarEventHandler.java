package com.mpagliaro98.mysubscriptions.ui.interfaces;

import java.util.Date;

/**
 * Interface to implement for classes that act as an event handler for the subscription
 * calendar. A CalendarEventHandler that is assigned as the calendar's event handler
 * will have its implemented method called whenever a date on the calendar is pressed.
 * This code is adapted from the CalendarView created by Ahmed Al-Amir (https://github.com/ahmed-alamir/CalendarView)
 */
public interface CalendarEventHandler {

    /**
     * Called when a date on the calendar is pressed.
     * @param date the date of the grid cell that is pressed
     */
    void onDayPress(Date date);
}
