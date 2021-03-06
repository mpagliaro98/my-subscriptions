package com.mpagliaro98.mysubscriptions.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SettingsManager;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * An activity for handling the creation of subscription objects. This builds off of the
 * subscription activity abstract class, using the same UI layout as the other subscription
 * classes.
 */
public class CreateSubscriptionActivity extends SubscriptionActivityAbstract {

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles class-specific changes that this class will make when the activity is
     * created. For the create version, this will auto-fill the date field with today's
     * date and remove the next payment date field.
     */
    @Override
    public void onCreateSubclass() {
        TextView date = findViewById(R.id.create_date);
        TextView nextDate = findViewById(R.id.create_next_date);
        setTitle(R.string.create_title_create);

        // Auto-fill the date field with the current date
        String dateFormat;
        try {
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            dateFormat = settingsManager.getDateFormat();
        } catch (IOException e) {
            dateFormat = getString(R.string.date_format_default);
        }
        date.setText(new SimpleDateFormat(dateFormat, Locale.US).format(new Date()));

        // Make sure the next date field can't be seen when creating
        ((ViewGroup)nextDate.getParent()).removeView(nextDate);
        View horizontal = findViewById(R.id.create_horizontal5);
        ((ViewGroup)horizontal.getParent()).removeView(horizontal);
        ConstraintLayout parentLayout = findViewById(R.id.create_constr_layout1);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parentLayout);
        float density = getResources().getDisplayMetrics().density;
        int marginPx = Math.round((float) 16 * density);
        constraintSet.connect(R.id.create_sublayout4, ConstraintSet.BOTTOM,
                R.id.create_constr_layout1, ConstraintSet.BOTTOM, marginPx);
        constraintSet.applyTo(parentLayout);
    }

    /**
     * Creates the action bar menu at the top of the page. For the create version of this
     * page, the edit and delete menu items will be hidden.
     * @param menu the menu to inflate
     * @return whether creation was successful or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_subscription, menu);

        // Set the two options to invisible when on the create page
        MenuItem editItem = menu.findItem(R.id.create_edit_button);
        editItem.setVisible(false);
        MenuItem deleteItem = menu.findItem(R.id.create_delete_button);
        deleteItem.setVisible(false);
        return true;
    }

    /**
     * Called when the create subscription button is pressed. Gets all the data from
     * each input field, creates the Subscription object, then sends it back to the
     * main activity.
     * @param view the current application view
     */
    @Override
    public void submitSubscription(View view) {
        // Get a subscription from the data in all the input fields, halt if data is invalid
        Subscription subscription = parseInputFields(view);
        if (subscription == null) {
            return;
        }

        // Put the object in the intent and send it to the tab activity
        Intent intent = MainActivity.buildGeneralMainIntent(this,
                MainActivity.INCOMING_TYPE.CREATE, subscription, -1, savedState);
        startActivity(intent);
    }

    /**
     * Method that provides functionality for when a back button is pressed. In the create
     * version of this page, this will just go back to the main activity and bring no
     * information with it.
     */
    @Override
    public void backButton() {
        Intent intent = MainActivity.buildGeneralMainIntent(this, null,
                null, -1, savedState);
        startActivity(intent);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // STATIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create a valid intent that can be used to access this activity. To access this activity,
     * several pieces of information need to be provided, which are specified by this
     * method.
     * @param context the current application context
     * @param savedState any saved state to be passed back eventually to the calling activity
     * @return a valid intent for accessing this activity
     */
    public static Intent buildGeneralCreateIntent(Context context, Bundle savedState) {
        Intent intent = new Intent(context, CreateSubscriptionActivity.class);
        intent.putExtra(MainActivity.SAVED_STATE_BUNDLE_MESSAGE, savedState);
        return intent;
    }
}
