package com.mpagliaro98.mysubscriptions.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The notification service checks each subscription in the model, then sends a notification
 * out if today is the user-specified number of days before the next payment date of any
 * given subscription. Future subscription payment dates are also updated here.
 */
public class NotificationService {

    private Context context;

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
    @RequiresApi(Build.VERSION_CODES.O)
    public void processBackgroundTasks() {
        // Get the notification manager, which will allow us to send notifications
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        assert notificationManager != null;

        // Create the notification channel so we can post to it
        createNotificationChannel();

        // Get the subscriptions from the file
        SharedViewModel model = new SharedViewModel();
        try {
            model.loadFromFile(context);
        } catch (IOException e) {
            sendIOExceptionNotif(notificationManager);
            return;
        }

        // Get today's date at 0:00:00 (so it matches with dates in subscriptions)
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        Date today = calendar.getTime();

        // Loop through each subscription
        List<Subscription> subList = model.getFullSubscriptionList();
        int notifCounter = 2;
        for (Subscription sub : subList) {
            // If this subscription's notification date is today, send the notification
            if (today.equals(sub.getNextNotifDate())) {
                createSubNotification(notificationManager, sub, notifCounter);
                notifCounter++;
            }
            // If today is after this sub's next payment date, update it
            if (today.after(sub.getNextPaymentDate())) {
                try {
                    updateSubDates(model, sub);
                } catch (IOException e) {
                    sendIOExceptionNotif(notificationManager);
                    return;
                }
            }
        }
    }

    /**
     * Helper method to create the notification channel needed to post notifications
     * on API level >= 26.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        // Create the channel with a name, description, and importance
        CharSequence name = context.getString(R.string.notification_channel_name);
        String description = context.getString(R.string.notification_channel_description);
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(name.toString(), name, importance);
        channel.setDescription(description);

        // Register the channel with the system
        NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
        assert notificationManager != null;
        notificationManager.createNotificationChannel(channel);
    }

    /**
     * Helper method that sends a notification indicating an error has occurred, specifically
     * some kind of IO error.
     * @param notificationManager the notification manager
     */
    private void sendIOExceptionNotif(NotificationManager notificationManager) {
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(context.getString(R.string.notification_ioexception_title))
                        .setContentText(context.getString(R.string.notification_ioexception_desc))
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setStyle(new NotificationCompat.BigTextStyle());
        notificationManager.notify(1, notification.build());
    }

    /**
     * Build and send a notification that indicates to the user when they're being charged
     * for a given subscription and for how much.
     * @param notificationManager the notification manager
     * @param sub the subscription that is being charged
     * @param notifCounter the ID of the current notification. Should be different each time
     */
    private void createSubNotification(NotificationManager notificationManager,
                                       Subscription sub, int notifCounter) {
        NotificationCompat.Builder notification =
                new NotificationCompat.Builder(context, context.getString(R.string.notification_channel_name))
                        .setSmallIcon(R.drawable.ic_launcher_background)
                        .setContentTitle(sub.getName() + " is being charged soon")
                        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                        .setStyle(new NotificationCompat.BigTextStyle());
        String messagePrefix = "";
        if (sub.getNotifDays() == 0) {
            messagePrefix = "Today, ";
        } else if (sub.getNotifDays() == 1) {
            messagePrefix = "Tomorrow, ";
        } else if (sub.getNotifDays() == 2) {
            messagePrefix = "In two days, ";
        } else if (sub.getNotifDays() == 3) {
            messagePrefix = "In three days, ";
        } else if (sub.getNotifDays() == 7) {
            messagePrefix = "In one week, ";
        } else {
            messagePrefix = "Soon, ";
        }
        String fullMessage = messagePrefix + "you'll be charged " + String.format("$%.2f", sub.getCost()) +
                " for " + sub.getName() + ".";
        notification.setContentText(fullMessage);
        notificationManager.notify(notifCounter, notification.build());
    }

    /**
     * Update the next payment date and next notification date of a subscription, then
     * save those changes back to the file.
     * @param model the model that holds all the subscriptions and their functionality
     * @param sub the subscription to update
     * @throws IOException thrown if something goes wrong while saving the file
     */
    private void updateSubDates(SharedViewModel model, Subscription sub) throws IOException {
        sub.generateNextPaymentDate();
        sub.generateNextNotifDate();
        model.updateSubscription(sub, sub.getId());
        model.saveToFile(context);
    }
}
