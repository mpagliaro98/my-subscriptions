package com.mpagliaro98.mysubscriptions.ui;

import androidx.annotation.NonNull;
import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SettingsManager;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The activity for managing settings. This will allow the settings to be modified and
 * saved or reset, which will be saved to a file and applied to other areas of the
 * application.
 */
public class SettingsActivity extends AppCompatActivity {

    // The saved state bundle from the previous activity
    private Bundle savedState;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * When this activity is created, initialize the data on the page.
     * @param savedInstanceState any saved instance state
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // Put the back button on this activity's title bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        // Saved the state from the previous activity so we can send it back when we return
        savedState = intent.getBundleExtra(MainActivity.SAVED_STATE_BUNDLE_MESSAGE);
        setTitle(R.string.settings_title);

        // Set the UI elements to their starting values based on existing settings
        try {
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            initializeUI(settingsManager);
        } catch (IOException e) {
            showErrorSnackbar(findViewById(android.R.id.content), getString(R.string.settings_snackbar_ioexception));
        }
    }

    /**
     * Called when any item on the action bar is selected. For this activity, this will only
     * be the back button, so this just sends control back to the main activity.
     * @param item the item that was selected
     * @return true if it was successful, false otherwise
     */
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Intent intent = MainActivity.buildGeneralMainIntent(this, null,
                null, -1, savedState);
        startActivity(intent);
        return super.onOptionsItemSelected(item);
    }

    /**
     * Called when the system back button is pressed. This will send control back to
     * the main activity.
     */
    @Override
    public void onBackPressed() {
        Intent intent = MainActivity.buildGeneralMainIntent(this, null,
                null, -1, savedState);
        startActivity(intent);
    }

    /**
     * Called when the save settings button is pressed. This gathers information from each
     * element in the UI, then sends them to the settings manager to be written to the
     * file.
     * @param view the current application view
     */
    public void saveSettings(View view) {
        try {
            // Get the settings manager and the UI elements holding the settings
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            Switch notifSwitch = findViewById(R.id.settings_notifications);
            TextView time = findViewById(R.id.settings_result_notiftime);
            Spinner currencyDropdown = findViewById(R.id.settings_currency_dropdown);
            Spinner dateFormatDropdown = findViewById(R.id.settings_date_format_dropdown);

            // Get the values from the UI
            boolean notificationsOn = notifSwitch.isChecked();
            Date notifTime;
            try {
                notifTime = new SimpleDateFormat(getString(R.string.time_format), Locale.US).parse(time.getText().toString());
            } catch (ParseException p) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, 6);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                notifTime = calendar.getTime();
            }
            String currencySymbol = (String)currencyDropdown.getSelectedItem();
            String dateFormat = (String)dateFormatDropdown.getSelectedItem();

            // Save the settings and display a success message if it works
            settingsManager.setSettings(notificationsOn, notifTime, currencySymbol,
                    dateFormat, getApplicationContext());
            MainActivity.setRecurringAlarm(getApplicationContext());
            Snackbar successBar = Snackbar.make(findViewById(android.R.id.content),
                    R.string.settings_snackbar_success, Snackbar.LENGTH_LONG);
            successBar.show();
        } catch (IOException e) {
            showErrorSnackbar(findViewById(android.R.id.content), getString(R.string.settings_snackbar_ioexception));
        }
    }

    /**
     * Called when the reset settings button is pressed, will display a dialog that when
     * confirmed, will set all the settings to their default values.
     * @param view the current application view
     */
    public void resetDefaults(View view) {
        try {
            final SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settings_reset_button))
                    .setMessage(getString(R.string.settings_reset_message))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            try {
                                settingsManager.resetToDefaults(getApplicationContext());
                                initializeUI(settingsManager);
                                MainActivity.setRecurringAlarm(getApplicationContext());
                                Snackbar.make(findViewById(android.R.id.content),
                                        R.string.settings_reset_success, Snackbar.LENGTH_LONG).show();
                            } catch (IOException e) {
                                showErrorSnackbar(findViewById(android.R.id.content), getString(R.string.settings_snackbar_ioexception));
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null).show();
        } catch (IOException e) {
            showErrorSnackbar(findViewById(android.R.id.content), getString(R.string.settings_snackbar_ioexception));
        }
    }

    /**
     * Called when the delete button is pressed, will display a dialog that when confirmed,
     * will delete all the user data (subscriptions) that the user has saved.
     * @param view the current application view
     */
    public void deleteData(View view) {
        try {
            final SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settings_delete_button))
                    .setMessage(getString(R.string.settings_delete_message))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (settingsManager.deleteSubscriptionData(getApplicationContext())) {
                                Snackbar.make(findViewById(android.R.id.content),
                                        R.string.settings_delete_success, Snackbar.LENGTH_LONG).show();
                            } else {
                                showErrorSnackbar(findViewById(android.R.id.content), getString(R.string.settings_snackbar_delete_error));
                            }
                        }
                    })
                    .setNegativeButton(R.string.no, null).show();
        } catch (IOException e) {
            showErrorSnackbar(findViewById(android.R.id.content), getString(R.string.settings_snackbar_ioexception));
        }
    }

    /**
     * Called when the delete synced calendar button is pressed, which will display a dialog
     * that when confirmed, will delete the data in the user's system calendar that was created
     * by this app.
     * @param view the current application view
     */
    public void deleteSyncedCalendar(View view) {
        try {
            final SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.settings_deletesync_button))
                    .setMessage(getString(R.string.settings_deletesync_message))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settingsManager.deleteSyncedCalendar(getApplicationContext());
                            initializeUI(settingsManager);
                            Snackbar.make(findViewById(android.R.id.content),
                                    R.string.settings_deletesync_success, Snackbar.LENGTH_LONG).show();
                        }
                    })
                    .setNegativeButton(R.string.no, null).show();
        } catch (IOException e) {
            showErrorSnackbar(findViewById(android.R.id.content), getString(R.string.settings_snackbar_ioexception));
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // STATIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create a valid intent that can be used to access this activity. All this activity
     * requires is the saved state from the previous activity, so it can be saved and
     * sent back later.
     * @param context the current application context
     * @param savedState a saved state bundle from the previous activity
     * @return the intent that can be used to access this activity
     */
    public static Intent buildGeneralSettingsIntent(Context context, Bundle savedState) {
        Intent intent = new Intent(context, SettingsActivity.class);
        intent.putExtra(MainActivity.SAVED_STATE_BUNDLE_MESSAGE, savedState);
        return intent;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Display an error message along the bottom of the screen saying an error occurred, and give
     * the option to reload the current view. This should be called when an IOException occurs
     * from accessing settings data.
     * @param view the view to display the Snackbar message in
     * @param errorMessage the string to display on the Snackbar
     */
    private void showErrorSnackbar(View view, String errorMessage) {
        Snackbar ioExceptionBar = Snackbar.make(view, errorMessage, Snackbar.LENGTH_INDEFINITE);
        ioExceptionBar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = buildGeneralSettingsIntent(getApplicationContext(), null);
                startActivity(intent);
            }
        });
        ioExceptionBar.show();
    }

    /**
     * Initialize all the UI components for settings values to the values that they are currently
     * set to in the settings manager.
     * @param settingsManager the settings manager object
     */
    private void initializeUI(final SettingsManager settingsManager) {
        Switch notifSwitch = findViewById(R.id.settings_notifications);
        ImageView timePicker = findViewById(R.id.settings_time_picker);
        final TextView time = findViewById(R.id.settings_result_notiftime);
        Spinner currencyDropdown = findViewById(R.id.settings_currency_dropdown);
        Spinner dateFormatDropdown = findViewById(R.id.settings_date_format_dropdown);

        // Set notifications to be on or off
        notifSwitch.setChecked(settingsManager.getNotificationsOn());

        // Create a time picker to be able to select the time notifications fire
        timePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // This calendar holds the hour and minute the time picker initializes to
                final Calendar calendar = Calendar.getInstance();
                try {
                    Date notifTime = new SimpleDateFormat(getString(R.string.time_format), Locale.US).parse(time.getText().toString());
                    assert notifTime != null;
                    calendar.setTime(notifTime);
                } catch (ParseException p) {
                    calendar.setTime(settingsManager.getNotificationTime());
                }
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                // Make the time picker
                TimePickerDialog picker = new TimePickerDialog(SettingsActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                // Update the time text field when the time picker is used
                                Calendar newCalendar = Calendar.getInstance();
                                newCalendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                                newCalendar.set(Calendar.MINUTE, minute);
                                newCalendar.set(Calendar.SECOND, 0);
                                newCalendar.set(Calendar.MILLISECOND, 0);
                                Date enteredTime = newCalendar.getTime();
                                time.setText(new SimpleDateFormat(getString(R.string.time_format),
                                        Locale.US).format(enteredTime));
                            }
                        }, hour, minute, true);
                picker.show();
            }
        });
        time.setText(new SimpleDateFormat(getString(R.string.time_format),
                Locale.US).format(settingsManager.getNotificationTime()));

        // Set the initial selection in the dropdown for currency symbols
        String[] currencyArray = getResources().getStringArray(R.array.array_currency);
        for (int i = 0; i < currencyArray.length; i++) {
            if (settingsManager.getCurrencySymbol().equals(currencyArray[i])) {
                currencyDropdown.setSelection(i);
            }
        }

        // Set the initial selection in the dropdown for date formats
        String[] dateFormatArray = getResources().getStringArray(R.array.array_date_format);
        for (int i = 0; i < dateFormatArray.length; i++) {
            if (settingsManager.getDateFormat().equals(dateFormatArray[i])) {
                dateFormatDropdown.setSelection(i);
            }
        }

        // If calendar permissions aren't granted or a sync calendar doesn't exist, remove the delete sync calendar button
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED ||
                !settingsManager.syncedCalendarExists(getApplicationContext())) {
            Button deleteSyncButton = findViewById(R.id.settings_deletesync);
            ((ViewGroup)deleteSyncButton.getParent()).removeView(deleteSyncButton);
            // Re-wire the constraints in the absence of this button
            ConstraintLayout parentLayout = findViewById(R.id.settings_base_constr_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(parentLayout);
            float density = getResources().getDisplayMetrics().density;
            int marginPx = Math.round((float) 8 * density);
            constraintSet.connect(R.id.settings_save, ConstraintSet.TOP,
                    R.id.settings_reset, ConstraintSet.BOTTOM, marginPx);
            constraintSet.applyTo(parentLayout);
        }
    }
}
