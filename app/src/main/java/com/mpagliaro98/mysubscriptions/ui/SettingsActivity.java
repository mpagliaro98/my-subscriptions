package com.mpagliaro98.mysubscriptions.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SettingsManager;
import java.io.IOException;
import java.util.Calendar;

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
        Switch notifSwitch = findViewById(R.id.settings_notifications);
        try {
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            notifSwitch.setChecked(settingsManager.getNotificationsOn());
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
    public boolean onOptionsItemSelected(MenuItem item) {
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
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            Switch notifSwitch = findViewById(R.id.settings_notifications);
            boolean notificationsOn = notifSwitch.isChecked();
            settingsManager.setSettings(notificationsOn, Calendar.getInstance().getTime(),
                    "$", getApplicationContext());
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
}
