package com.mpagliaro98.mysubscriptions.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import com.mpagliaro98.mysubscriptions.R;

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
}
