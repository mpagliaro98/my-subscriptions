package com.mpagliaro98.mysubscriptions.ui.interfaces;

/**
 * Fragments that implement this interface can become sync calendar listeners for MainActivity.
 * A sync calendar listener will have the syncCalendar method called when the sync calendar
 * button is pressed, and that class will handle the work.
 */
public interface OnSyncCalendarListener {

    /**
     * Called when the button to sync the calendar is pressed. This will assert that the app
     * has permission to use the calendar API, then create the calendar on the system and
     * create events on the calendar for each subscription's payment dates.
     */
    void syncCalendar();
}
