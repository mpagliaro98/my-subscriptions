package com.mpagliaro98.mysubscriptions.ui.interfaces;

import androidx.annotation.NonNull;

/**
 * Fragments that implement this interface can become sync calendar listeners for MainActivity.
 * A sync calendar listener will have the syncCalendar method called when the sync calendar
 * button is pressed, and that class will handle the work.
 */
public interface OnSyncCalendarListener {

    // Possible end codes for the calendar sync, should be send to handleSyncResult
    int SYNC_THREAD_SUCCESS = 0;
    int SYNC_THREAD_SECURITY_EXCEPTION = 1;

    /**
     * Called when the button to sync the calendar is pressed. This will assert that the app
     * has permission to use the calendar API, then create the calendar on the system and
     * create events on the calendar for each subscription's payment dates.
     */
    void syncCalendar();

    /**
     * Handles the outcome of requesting a permission. After the calendar permissions are
     * requested, the onRequestPermissionsResult method can call this to handle the
     * result.
     * @param permissions an array of permissions that were requested
     * @param grantResults an array of integers showing the results of those permission requests
     */
    void handleRequestResult(@NonNull String[] permissions, @NonNull int[] grantResults);

    /**
     * Handles the outcome of the calendar sync thread. When the process of syncing the
     * calendar is run on a separate thread, that thread should keep an instance of this
     * class with it and call this method when it's done. The result it sends should be
     * one of the SYNC_THREAD_... integer codes in the interface.
     * @param result a sync thread code denoting how the process finished
     */
    void handleSyncResult(int result);
}
