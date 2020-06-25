package com.mpagliaro98.mysubscriptions.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;

/**
 * An activity for handling the editing of subscription objects. This builds off of the
 * subscription activity abstract class, using the same UI layout as the other subscription
 * classes.
 */
public class EditSubscriptionActivity extends SubscriptionActivityAbstract {

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles class-specific changes that this class will make when the activity is
     * created. For the edit version, this will fill each UI field with the given
     * subscription's data, then make sure the next payment date field isn't seen.
     */
    @Override
    public void onCreateSubclass() {
        TextView name = findViewById(R.id.create_name);
        TextView cost = findViewById(R.id.create_cost);
        TextView note = findViewById(R.id.create_note);
        Spinner frequency = findViewById(R.id.create_freq_dropdown);
        TextView nextDate = findViewById(R.id.create_next_date);
        Button createButton = findViewById(R.id.create_button_finish);
        Spinner notifications = findViewById(R.id.create_notif_dropdown);
        TextView date = findViewById(R.id.create_date);
        Spinner category = findViewById(R.id.create_category_dropdown);
        assert sub != null;
        setTitle(R.string.create_title_edit);

        // Fill every field with the values of the subscription to edit
        createButton.setText(R.string.create_button_edit);
        name.setText(sub.getName());
        cost.setText(String.valueOf(sub.getCost()));
        date.setText(sub.getStartDateString(getApplicationContext()));
        note.setText(sub.getNote());

        // Set the recharge dropdown to this subscription's value
        frequency.setSelection(getRechargeDropdownSelection(sub.getRechargeFrequency()));

        // Set the category dropdown to this subscription's category
        category.setSelection(getCategoryDropdownSelection(sub.getCategory()));

        // Set the notifications dropdown to the proper value
        notifications.setSelection(getNotifDropdownSelection(sub.getNotifDays()));

        // Make sure the next date field can't be seen when editing
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
     * Creates the action bar menu at the top of the page. For the edit version of this
     * page, the edit menu item will be hidden.
     * @param menu the menu to inflate
     * @return whether creation was successful or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_subscription, menu);

        // Set the edit option to invisible when on the edit page
        MenuItem editItem = menu.findItem(R.id.create_edit_button);
        editItem.setVisible(false);
        return true;
    }

    /**
     * Called when the update subscription button is pressed. Gets all the data from
     * each input field, creates the Subscription object, then sends it back to the
     * tab activity.
     * @param view the current application view
     */
    @Override
    public void submitSubscription(View view) {
        // Get a subscription from the data in all the input fields, halt if data is invalid
        Subscription subscription = parseInputFields(view);
        if (subscription == null) {
            return;
        }

        // Put the object and its index in the intent and send it to the tab activity
        Intent intent = MainActivity.buildGeneralMainIntent(this,
                MainActivity.INCOMING_TYPE.EDIT, subscription, subIndex, savedState);
        startActivity(intent);
    }

    /**
     * Method that provides functionality for when a back button is pressed. In the edit
     * version of this page, this will go back to the view version of this page, using the
     * same subscription data it came here with.
     */
    @Override
    public void backButton() {
        Intent intent = ViewSubscriptionActivity.buildGeneralViewIntent(this, sub,
                subIndex, savedState);
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
     * @param subscription the subscription object that will be visible in the UI fields
     * @param subIndex the ID of the subscription
     * @param savedState any saved state to be passed back eventually to the calling activity
     * @return a valid intent for accessing this activity
     */
    public static Intent buildGeneralEditIntent(Context context, Subscription subscription,
                                                int subIndex, Bundle savedState) {
        Intent intent = new Intent(context, EditSubscriptionActivity.class);
        intent.putExtra(VIEW_SUB_MESSAGE, subscription);
        intent.putExtra(SUB_ID_MESSAGE, subIndex);
        intent.putExtra(MainActivity.SAVED_STATE_BUNDLE_MESSAGE, savedState);
        return intent;
    }
}
