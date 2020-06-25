package com.mpagliaro98.mysubscriptions.ui;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;

/**
 * An activity for handling the viewing of subscription objects. This builds off of the
 * subscription activity abstract class, using the same UI layout as the other subscription
 * classes.
 */
public class ViewSubscriptionActivity extends SubscriptionActivityAbstract {

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Handles class-specific changes that this class will make when the activity is
     * created. For the view version, this will fill all the UI fields and make each
     * field non-editable.
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
        ImageView datePicker = findViewById(R.id.create_date_picker);
        Spinner category = findViewById(R.id.create_category_dropdown);
        assert sub != null;
        setTitle(R.string.create_title_view);

        // Set each field to be non-editable
        name.setLinksClickable(false);
        name.setCursorVisible(false);
        name.setFocusableInTouchMode(false);
        cost.setLinksClickable(false);
        cost.setCursorVisible(false);
        cost.setFocusableInTouchMode(false);
        date.setLinksClickable(false);
        date.setCursorVisible(false);
        date.setFocusableInTouchMode(false);
        datePicker.setVisibility(View.INVISIBLE);
        note.setLinksClickable(false);
        note.setCursorVisible(false);
        note.setFocusableInTouchMode(false);

        // Set the recharge dropdown to this subscription's value
        frequency.setEnabled(false);
        frequency.setSelection(getRechargeDropdownSelection(sub.getRechargeFrequency()));

        // Write the next payment date to the screen
        String nextDateStr = getString(R.string.create_text_next_date) + " " +
                sub.getNextPaymentDateString(getApplicationContext());
        nextDate.setText(nextDateStr);

        // Set the category dropdown to this subscription's category
        category.setEnabled(false);
        category.setSelection(getCategoryDropdownSelection(sub.getCategory()));

        // Set the notifications dropdown to the proper value
        notifications.setEnabled(false);
        notifications.setSelection(getNotifDropdownSelection(sub.getNotifDays()));

        // Remove the button from the page
        ((ViewGroup)createButton.getParent()).removeView(createButton);
        ConstraintLayout parentLayout = findViewById(R.id.create_base_constr_layout);
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(parentLayout);
        float density = getResources().getDisplayMetrics().density;
        int marginPx = Math.round((float) 8 * density);
        constraintSet.connect(R.id.create_constr_layout2, ConstraintSet.BOTTOM,
                R.id.create_base_constr_layout, ConstraintSet.BOTTOM, marginPx);
        constraintSet.applyTo(parentLayout);

        // Fill every field with the values of the subscription to view
        name.setText(sub.getName());
        cost.setText(sub.getCostString(getApplicationContext()));
        date.setText(sub.getStartDateString(getApplicationContext()));
        note.setText(sub.getNote());
    }

    /**
     * Method that provides functionality for when a back button is pressed. In the view
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
     * @param subscription the subscription object that will be visible in the UI fields
     * @param subIndex the ID of the subscription
     * @param savedState any saved state to be passed back eventually to the calling activity
     * @return a valid intent for accessing this activity
     */
    public static Intent buildGeneralViewIntent(Context context, Subscription subscription,
                                                  int subIndex, Bundle savedState) {
        Intent intent = new Intent(context, ViewSubscriptionActivity.class);
        intent.putExtra(VIEW_SUB_MESSAGE, subscription);
        intent.putExtra(SUB_ID_MESSAGE, subIndex);
        intent.putExtra(MainActivity.SAVED_STATE_BUNDLE_MESSAGE, savedState);
        return intent;
    }
}
