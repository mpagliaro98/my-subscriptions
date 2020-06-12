package com.mpagliaro98.mysubscriptions.model;

import android.content.Context;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Date;

/**
 * A class to hold application-wide settings. Simply creating a new instance of this class
 * will load the settings from the file and be able to be accessed.
 */
public class SettingsManager {

    // The fields for each setting
    private boolean notificationsOn;
    private Date notificationTime;
    private String currencySymbol;
    // The name of the file the settings are stored in
    private static final String filename = "settings.dat";

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Creates this class. This will load the settings from the file. If no file exists yet,
     * the default settings will be instantiated and a file containing them will be created.
     * @param context the current application context
     * @throws IOException thrown if there is an error reading or writing to the file
     */
    public SettingsManager(Context context) throws IOException {
        setDefaults();
        loadSettingsFile(context);
    }

    /**
     * Get whether notifications should go off or not.
     * @return the notification setting as a boolean
     */
    public boolean getNotificationsOn() {
        return notificationsOn;
    }

    /**
     * Get the time of day notifications should fire at.
     * @return the time of day for notifications as a Date object
     */
    public Date getNotificationTime() {
        return notificationTime;
    }

    /**
     * Get the symbol that should precede currency values.
     * @return the currency symbol as a string
     */
    public String getCurrencySymbol() {
        return currencySymbol;
    }

    /**
     * Set the settings to the values that are passed in, then save them all to the file. This
     * should ideally be called when a "save" button is pressed in the settings activity, where
     * all the settings can be gathered from their respective UI elements.
     * @param notificationsOn the notification setting as a boolean
     * @param notificationTime the time of day for notifications as a Date object
     * @param currencySymbol the currency symbol as a string
     * @param context the current application context
     * @throws IOException thrown if there is an error reading or writing to the file
     */
    public void setSettings(boolean notificationsOn, Date notificationTime, String currencySymbol,
                            Context context) throws IOException {
        this.notificationsOn = notificationsOn;
        this.notificationTime = notificationTime;
        this.currencySymbol = currencySymbol;
        saveSettingsFile(context);
    }

    /**
     * Resets all the settings to their default values, then overwrites the settings
     * file to save the changes.
     * @param context the current application context
     * @throws IOException thrown if there's an error writing to the file
     */
    public void resetToDefaults(Context context) throws IOException {
        setDefaults();
        saveSettingsFile(context);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Reset the settings to their default values. This does not save over the file.
     */
    private void setDefaults() {
        notificationsOn = true;
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        notificationTime = calendar.getTime();
        currencySymbol = "$";
    }

    /**
     * Load the settings from the file and save it to this object's fields. Return before
     * any reads if the file does not exist.
     * @param context the current application context
     * @throws IOException thrown if there is an issue reading the file
     */
    private void loadSettingsFile(Context context) throws IOException {
        // Don't do anything if the internal file doesn't exist
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) {
            return;
        }

        // Make the reader and open the file
        Gson gson = new Gson();
        FileInputStream fis = context.openFileInput(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        // Read each line and save them to their respective fields
        String line = reader.readLine();
        notificationsOn = gson.fromJson(line, Boolean.class);
        line = reader.readLine();
        notificationTime = gson.fromJson(line, Date.class);
        line = reader.readLine();
        currencySymbol = gson.fromJson(line, String.class);
        reader.close();
    }

    /**
     * Save the settings to the file. This will delete the file and recreate it if it
     * already exists, then write the settings to it.
     * @param context the current application context
     * @throws IOException thrown if there's an error writing the file
     */
    private void saveSettingsFile(Context context) throws IOException {
        // Refresh the file so we write to it from scratch
        Gson gson = new Gson();
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException();
            }
        }
        if (!file.createNewFile()) {
            throw new IOException();
        }

        // Convert each field to json, then add it to the file line by line
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        String line = gson.toJson(notificationsOn) + "\n";
        fos.write(line.getBytes());
        line = gson.toJson(notificationTime) + "\n";
        fos.write(line.getBytes());
        line = gson.toJson(currencySymbol) + "\n";
        fos.write(line.getBytes());
        fos.close();
    }
}