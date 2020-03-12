package com.mpagliaro98.mysubscriptions.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.mpagliaro98.mysubscriptions.R;

/**
 * The notification service checks each subscription in the model, then sends a notification
 * out if today is the user-specified number of days before the next payment date of any
 * given subscription.
 */
public class NotificationService extends Service {

    /**
     * Since this service doesn't communicate with any other components, this
     * can just return null.
     * @param intent A passed in intent
     * @return null
     */
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    /**
     * Called when the service is created, as long as the API level is above
     * 26 (due to certain notification features) control will be passed to the
     * method that creates notifications.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            createSubNotifications();
    }

    /**
     * Main method to create notifications for the subscriptions that require them
     * on any given day.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createSubNotifications() {
        // Create the notification channel so we can post to it
        createNotificationChannel();

        // TODO here check which subscriptions should receive notifications
        // use this to also update payment dates

        // Get the notification manager, which will allow us to send notifications
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        assert notificationManager != null;

        // TEST: build a test notification and send it, displays regardless of date
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getApplicationContext(), getString(R.string.notification_channel_name))
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle("My notification")
                        .setContentText("Hello World!")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        notificationManager.notify(1, mBuilder.build());
    }

    /**
     * Helper method to create the notification channel needed to post notifications
     * on API level >= 26.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        // Create the channel with a name, description, and importance
        CharSequence name = getString(R.string.notification_channel_name);
        String description = getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(
                getString(R.string.notification_channel_name), name, importance);
        channel.setDescription(description);

        // Register the channel with the system
        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }
}
