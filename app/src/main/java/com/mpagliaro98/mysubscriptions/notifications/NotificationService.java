package com.mpagliaro98.mysubscriptions.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SettingsManager;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.model.ZeroTimeCalendar;
import com.mpagliaro98.mysubscriptions.ui.CreateSubscriptionActivity;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * The notification service checks each subscription in the model, then sends a notification
 * out if today is the user-specified number of days before the next payment date of any
 * given subscription. Future subscription payment dates are also updated here.
 */
public class NotificationService {

    private static final String TAG = "NotificationService";

    private Context context;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create this class and give it the context to use later when creating
     * notifications and accessing the subscription file.
     * @param context the current application context
     */
    public NotificationService(Context context) {
        this.context = context;
    }

    /**
     * Main method to create notifications for the subscriptions that require them
     * on any given day and update any subscription dates.
     */
    public void processBackgroundTasks() {
        processBackgroundTasks(new ZeroTimeCalendar());
    }
    /**
     * Main method to create notifications for the subscriptions that require them
     * on any given day and update any subscription dates.
     * @param zeroTimeCalendar a calendar of today's date with the time set to 0:00:00
     */
    public void processBackgroundTasks(ZeroTimeCalendar zeroTimeCalendar) {
        // Get the notification manager, which will allow us to send notifications
        NotificationManager notificationManager;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            // Create the notification channel so we can post to it
            createNotificationChannel(notificationManager);
            Log.i(TAG, "Notification channel created successfully");
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
        } else {
            notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            assert notificationManager != null;
        }

        // If the settings say notifications are off, stop here
        try {
            SettingsManager settingsManager = new SettingsManager(context);
            if (!settingsManager.getNotificationsOn()) {
                return;
            }
        } catch (IOException e) {
            sendIOExceptionNotif(notificationManager);
            return;
        }

        // Get the subscriptions from the file and update any dates as necessary
        SharedViewModel model = new SharedViewModel();
        try {
            model.loadFromFile(context);
            int numUpdated = model.updateSubscriptionDates();
            if (numUpdated > 0) {
                model.saveToFile(context);
            }
        } catch (IOException e) {
            sendIOExceptionNotif(notificationManager);
            return;
        }
        Log.i(TAG, "Subscriptions successfully loaded from file");

        // Get today's date at 0:00:00 (so it matches with dates in subscriptions)
        Date today = zeroTimeCalendar.getCurrentDate();

        // Loop through each subscription
        List<Subscription> subList = model.getFullSubscriptionList();
        List<Subscription> subsWithNotifications = new ArrayList<>();
        for (Subscription sub : subList) {
            // If this subscription's notification date is today, add it to the list
            if (today.equals(sub.getNextNotifDate())) {
                Log.i(TAG, sub.getName() + " will be added to the notification");
                subsWithNotifications.add(sub);
            }
        }

        // If there's subscriptions in the notify list, create a notification for them
        if (!subsWithNotifications.isEmpty()) {
            createSubNotification(notificationManager, subsWithNotifications);
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Helper method to create the notification channel needed to post notifications
     * on API level >= 26.
     * @param notificationManager the notification manager
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel(NotificationManager notificationManager) {
        // Create the channel with a name, description, and importance
        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
        channel.setDescription(description);

        // Register the channel with the system
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Helper method that sends a notification indicating an error has occurred, specifically
     * some kind of IO error.
     * @param notificationManager the notification manager
     */
    private void sendIOExceptionNotif(NotificationManager notificationManager) {
        // Setup the intent to launch the home page when this notification is selected
        Intent intent = MainActivity.buildGeneralMainIntent(context, null,
                null, -1, null);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the actual notification
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(context.getString(R.string.notification_ioexception_title))
                        .setContentText(context.getString(R.string.notification_ioexception_desc))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
        notificationManager.notify(1, notification.build());
    }

    /**
     * Build and send a notification that indicates to the user when they're being charged
     * for a given subscription and for how much. If multiple subscriptions need to be notified,
     * it will send one notification that list all of those subscriptions at once.
     * @param notificationManager the notification manager
     * @param subscriptions the list of subscriptions to generate notifications for
     */
    private void createSubNotification(NotificationManager notificationManager,
                                       List<Subscription> subscriptions) {
        // If there's multiple subscriptions, launch the homepage, otherwise launch the subscription
        Intent intent;
        if (subscriptions.size() == 1) {
            intent = CreateSubscriptionActivity.buildGeneralCreateIntent(context,
                    CreateSubscriptionActivity.PAGE_TYPE.VIEW, subscriptions.get(0),
                    subscriptions.get(0).getId(), null);
        } else {
            intent = MainActivity.buildGeneralMainIntent(context, null,
                    null, -1, null);
        }
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntentWithParentStack(intent);
        PendingIntent pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

        // Build the actual notification
        String contentTitle = subscriptions.size() == 1 ?
                subscriptions.get(0).getName() + " " + context.getString(R.string.notification_title_one)
                : context.getString(R.string.notification_title_multiple);
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setContentTitle(contentTitle)
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setStyle(new NotificationCompat.BigTextStyle())
                        .setContentText(generateContextText(subscriptions))
                        .setContentIntent(pendingIntent)
                        .setAutoCancel(true);
        notificationManager.notify(2, notification.build());
    }

    /**
     * Build the content text for the subscription notification by generating a short sentence
     * for each due subscription and concatenating them together.
     * @param subscriptions the list of subscriptions to generate notifications for
     * @return a string with information on every due subscription, separated by newlines
     */
    private String generateContextText(List<Subscription> subscriptions) {
        StringBuilder stringBuilder = new StringBuilder();
        for (Subscription sub : subscriptions) {
            if (sub.getNotifDays() == 0) {
                stringBuilder.append(context.getString(R.string.notification_content_days_zero));
            } else if (sub.getNotifDays() == 1) {
                stringBuilder.append(context.getString(R.string.notification_content_days_one));
            } else if (sub.getNotifDays() == 2) {
                stringBuilder.append(context.getString(R.string.notification_content_days_two));
            } else if (sub.getNotifDays() == 3) {
                stringBuilder.append(context.getString(R.string.notification_content_days_three));
            } else if (sub.getNotifDays() == 7) {
                stringBuilder.append(context.getString(R.string.notification_content_days_seven));
            } else {
                stringBuilder.append(context.getString(R.string.notification_content_days_default));
            }
            stringBuilder.append(' ');
            stringBuilder.append(context.getString(R.string.notification_content_build_1));
            stringBuilder.append(' ');
            stringBuilder.append(sub.getCostString(context.getResources()));
            stringBuilder.append(' ');
            stringBuilder.append(context.getString(R.string.notification_content_build_2));
            stringBuilder.append(' ');
            stringBuilder.append(sub.getName());
            stringBuilder.append(".\n");
        }
        return stringBuilder.toString().trim();
    }
}
