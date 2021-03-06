package com.mpagliaro98.mysubscriptions.notifications;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;

/**
 * This class receives a broadcast that will be made from the AlarmManager each day,
 * and this will pass on control to the notification service.
 */
public class AlarmReceiver extends BroadcastReceiver {

    public static final String intentAction = "com.mpagliaro98.action.NOTIFICATIONS";
    private static final String TAG = "AlarmReceiver";

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Called at the time specified in setRecurringAlarm in MainActivity, when once a day
     * this will use NotificationService to create notifications and update subscriptions.
     * This is also called when the device is turned on, which will reset the alarm so
     * notifications continue to run at the proper time.
     * @param context The phone's current context (can be called when the app is closed)
     * @param intent The pending intent (either from MainActivity or when boot completes)
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "Receiver called");
        if (intent.getAction() != null) {
            // If the intent is from this application, create notifications
            if (intent.getAction().equals(intentAction)) {
                Log.i(TAG, "Standard application intent, running notifications");
                NotificationService notificationService = new NotificationService(context);
                notificationService.processBackgroundTasks();
            }
            // If the intent signals the phone was turned on, re-activate the notification alarm
            else if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
                Log.i(TAG, "Boot received, setting alarm");
                MainActivity.setRecurringAlarm(context);
            }
            else {
                Log.w(TAG, "The intent sent to AlarmReceiver was not recognized");
            }
        } else {
            Log.w(TAG, "The intent sent to AlarmReceiver was null");
        }
    }
}
