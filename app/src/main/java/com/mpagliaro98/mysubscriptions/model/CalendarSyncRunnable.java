package com.mpagliaro98.mysubscriptions.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CalendarContract;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.ui.interfaces.OnSyncCalendarListener;
import java.util.Date;
import java.util.TimeZone;

/**
 * A thread class that runs the code to sync this app's model data with the calendar of the
 * device this is running on.
 */
public class CalendarSyncRunnable extends Thread {

    // Context, model, and calling class needed to sync the calendar
    private Context context;
    private SharedViewModel model;
    private OnSyncCalendarListener caller;

    // The account type used to create the sync calendar
    static final String CALENDAR_ACCOUNT_TYPE = "com.mpagliaro98";
    // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance
    static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };
    // The indices for the projection array above
    private static final int PROJECTION_ID_INDEX = 0;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create the thread class. This will save the application context and model so that
     * they can be used while the thread is running.
     * @param context the current application context
     * @param model the model containing all subscription data
     */
    public CalendarSyncRunnable(Context context, SharedViewModel model, OnSyncCalendarListener caller) {
        this.context = context;
        this.model = model;
        this.caller = caller;
    }

    /**
     * The code to run when the thread is running. This will delete the calendar if one was
     * created during a previous sync, then create a new calendar, then add all of the events
     * to it. Calendar API permissions must have been requested and accepted prior to this
     * thread being run.
     */
    @Override
    public void run() {
        // Delete the existing version of this calendar on the system and recreate it
        deleteSyncCalendar(context);
        createSyncCalendar(context, context.getString(R.string.calendar_sync_name));

        // Create the query to find the created calendar and its ID
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {context.getString(R.string.app_name), CALENDAR_ACCOUNT_TYPE,
                context.getString(R.string.app_name)};

        try {
            // Submit the query and get a Cursor object back
            Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

            // Use the cursor to step through the returned records (should just be the one calendar)
            assert cur != null;
            while (cur.moveToNext()) {
                // Get the calendar ID
                int calID = (int)cur.getLong(PROJECTION_ID_INDEX);

                // Loop through each subscription and create events for each of their payment dates
                for (Subscription sub : model.getFullSubscriptionList()) {
                    createSyncCalendarEvents(context, calID, sub);
                }
            }
            cur.close();

            // Send the success code to the caller's handler
            caller.handleSyncResult(OnSyncCalendarListener.SYNC_THREAD_SUCCESS);
        } catch (SecurityException e) {
            // If a security exception occurs, send the error code to the caller's handler
            caller.handleSyncResult(OnSyncCalendarListener.SYNC_THREAD_SECURITY_EXCEPTION);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create a calendar on the system calendar that this app can add events to.
     * @param context the current application context
     * @param name the name of the calendar
     */
    private void createSyncCalendar(Context context, String name) {
        // Create the target URI
        Uri target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        target = target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, context.getString(R.string.app_name))
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDAR_ACCOUNT_TYPE).build();

        // Add all the necessary values for it
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, context.getString(R.string.app_name));
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDAR_ACCOUNT_TYPE);
        values.put(CalendarContract.Calendars.NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, context.getResources().getColor(R.color.colorPrimary));
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, context.getString(R.string.app_name));
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().toString());
        values.put(CalendarContract.Calendars.CAN_PARTIALLY_UPDATE, 1);

        // Create the calendar
        context.getContentResolver().insert(target, values);
    }

    /**
     * Delete the system calendar that was created by this app, if it exists. If it doesn't
     * exist, this won't do anything.
     * @param context the current application context
     */
    private void deleteSyncCalendar(Context context) {
        Uri uri = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        uri = uri.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, context.getString(R.string.app_name))
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDAR_ACCOUNT_TYPE).build();
        context.getContentResolver().delete(uri, null, null);
    }

    /**
     * Add a set of events to the system calendar. This will loop through the next payment dates
     * for the given subscription and add an event to the calendar for each one.
     * @param context the current application context
     * @param calID the ID of the calendar the events will be added to
     * @param subscription the subscription to get next payment dates from
     * @throws SecurityException thrown if the app doesn't have permission to create events
     */
    private void createSyncCalendarEvents(Context context, int calID, Subscription subscription) throws SecurityException {
        for (Date paymentDate : subscription.getNextPaymentList()) {
            // Fill the content values with all the info needed for this event
            ContentValues cv = new ContentValues();
            cv.put(CalendarContract.Events.TITLE, subscription.getName() + " " + context.getString(R.string.calendar_sync_name_suffix));
            cv.put(CalendarContract.Events.DTSTART, paymentDate.getTime());
            cv.put(CalendarContract.Events.DTEND, paymentDate.getTime());
            cv.put(CalendarContract.Events.CALENDAR_ID, calID);
            cv.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
            cv.put(CalendarContract.Events.ALL_DAY, true);

            // Create the event
            context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, cv);
        }
    }
}
