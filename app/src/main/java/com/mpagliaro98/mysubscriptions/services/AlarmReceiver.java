package com.mpagliaro98.mysubscriptions.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

/**
 * This class receives a broadcast that will be made from the AlarmManager each day,
 * and this will pass on control to the notification service.
 */
public class AlarmReceiver extends BroadcastReceiver {

    // TODO have the service restart on boot
    /**
     * Called at the time specified in setRecurringAlarm in FragmentHome, when once a day
     * this will start the notification service, which will post notifications for any
     * upcoming subscriptions.
     * @param context The phone's current context (can be called when the app is closed)
     * @param intent The pending intent set in setRecurringAlarm
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Receiver called", Toast.LENGTH_SHORT).show();
        context.startService(new Intent(context, NotificationService.class));
    }
}
