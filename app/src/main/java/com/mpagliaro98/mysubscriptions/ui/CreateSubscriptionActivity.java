package com.mpagliaro98.mysubscriptions.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * An activity for gathering all the data on a new subscription to create, compiling
 * all the data into a subscription object, then sending it back to the tab activity
 * so it can be added to the model.
 */
public class CreateSubscriptionActivity extends AppCompatActivity {

    private static final String dateFormat = "MM/dd/yyyy";
    public static final String PAGE_TYPE_MESSAGE = "com.mpagliaro98.mysubscriptions.PAGE_TYPE";
    public static final String VIEW_SUB_MESSAGE = "com.mpagliaro98.mysubscriptions.VIEW_SUB";

    // Different versions this page can be, send this to this page each time it is accessed
    public enum PAGE_TYPE {CREATE, EDIT, VIEW};

    // The current state of this page
    private PAGE_TYPE pageType;

    /**
     * When this activity is created, initialize it and load any data we need. Set the
     * page mode to either create, edit, or view, and set the fields on the view to their
     * starting values.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_subscription);

        // Pre-load each field from the page so we can easily modify them
        TextView name = findViewById(R.id.create_name);
        TextView cost = findViewById(R.id.create_cost);
        TextView date = findViewById(R.id.create_date);
        TextView note = findViewById(R.id.create_note);
        Button createButton = findViewById(R.id.create_button_finish);
        // This incoming subscription will be null when page type is CREATE
        Subscription sub = (Subscription)getIntent().getSerializableExtra(VIEW_SUB_MESSAGE);

        // Display a different version of this page depending on the parameter passed in
        Intent intent = getIntent();
        pageType = (PAGE_TYPE)intent.getSerializableExtra(PAGE_TYPE_MESSAGE);
        // For create, set the page to the create version with editable, empty fields
        if (pageType == PAGE_TYPE.CREATE) {
            // Auto-fill the date field with the current date, properly formatted
            date.setText(new SimpleDateFormat(dateFormat, Locale.getDefault()).format(new Date()));
        }
        // For view, populate the page with un-selectable text fields for each subscription field
        else if (pageType == PAGE_TYPE.VIEW) {
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
            note.setLinksClickable(false);
            note.setCursorVisible(false);
            note.setFocusableInTouchMode(false);
            createButton.setVisibility(View.INVISIBLE);

            // Fill every field with the values of the subscription to view
            name.setText(sub.getName());
            cost.setText(String.format("$%.2f", sub.getCost()));
            date.setText(new SimpleDateFormat(dateFormat, Locale.US).format(sub.getStartDate()));
            note.setText(sub.getNote());
        }
        else if (pageType == PAGE_TYPE.EDIT) {
            // Fill every field with the values of the subscription to edit
            createButton.setText(R.string.create_button_edit);
            name.setText(sub.getName());
            cost.setText(String.format("%.2f", sub.getCost()));
            date.setText(new SimpleDateFormat(dateFormat, Locale.US).format(sub.getStartDate()));
            note.setText(sub.getNote());
        }
    }

    /**
     * Creates the action bar menu at the top of the page.
     * @param menu the menu to create
     * @return whether creation was successful or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_subscription, menu);

        // If we aren't in view mode, set the edit button at the top to not display
        if (pageType != PAGE_TYPE.VIEW) {
            MenuItem item = menu.findItem(R.id.create_edit_button);
            item.setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called when the create subscription button is pressed. Gets all the data from
     * each input field, creates the Subscription object, then sends it back to the
     * tab activity.
     * @param view the current application view
     */
    public void createSubscription(View view) {
        if (pageType == PAGE_TYPE.CREATE) {
            // Get a subscription from the data in all the input fields, halt if data is invalid
            Subscription subscription = parseInputFields(view);
            if (subscription == null) {
                return;
            }

            // Put the object in the intent and send it to the tab activity
            Intent intent = new Intent(this, HomeTabActivity.class);
            intent.putExtra(HomeTabActivity.SUBSCRIPTION_MESSAGE, subscription);
            startActivity(intent);
        }
        else if (pageType == PAGE_TYPE.EDIT) {
            // TODO add functionality for edit mode
            displayErrorBar(view, R.string.app_name);
        }
    }

    /**
     * Called when a button on the action bar is pressed. When the edit button is pressed
     * in this activity, we will reload the page in edit mode, sending the subscription
     * currently being viewed through the intent.
     * @param item the item on the action bar that was pressed
     * @return whether it completed successfully or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.create_edit_button) {
            Intent intent = new Intent(this, CreateSubscriptionActivity.class);
            intent.putExtra(CreateSubscriptionActivity.PAGE_TYPE_MESSAGE,
                            PAGE_TYPE.EDIT);
            Subscription subscription = parseInputFields(null);
            intent.putExtra(CreateSubscriptionActivity.VIEW_SUB_MESSAGE,
                            subscription);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Display a small bar on the bottom of the screen. Should be called when there is
     * something to display regarding a recent action (like improperly formatted input
     * when submitting something).
     * @param view the current view of the application
     * @param stringId the string to display, should be in strings.xml
     */
    private void displayErrorBar(View view, int stringId) {
        Snackbar.make(view, stringId, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Parse the input fields in the current view and build a subscription object from
     * them. This will display an error message and return null if one of the fields is
     * invalid.
     * @param view the current application view, if null then error bars won't display
     * @return a Subscription object if everything is valid, null otherwise
     */
    private Subscription parseInputFields(View view) {
        // Get each input field from the view
        EditText nameText = findViewById(R.id.create_name);
        EditText dateText = findViewById(R.id.create_date);
        EditText costText = findViewById(R.id.create_cost);
        EditText noteText = findViewById(R.id.create_note);

        // Extract the data from name and validate it
        String name = nameText.getText().toString();
        if (name.equals("")) {
            if (view != null) {
                displayErrorBar(view, R.string.create_error_name);
            }
            return null;
        }

        // Extract the data from date and validate it
        Date date;
        try {
            date = new SimpleDateFormat(dateFormat, Locale.US).parse(dateText.getText().toString());
        } catch (ParseException e) {
            if (view != null) {
                displayErrorBar(view, R.string.create_error_date);
            }
            return null;
        }

        // Extract the data from cost and validate it, remove the currency symbol if it's there
        double cost;
        String costTemp = costText.getText().toString();
        if (costTemp.startsWith("$")) {
            costTemp = costTemp.substring(1);
        }
        try {
            cost = Double.parseDouble(costTemp);
        } catch (NumberFormatException e) {
            if (view != null) {
                displayErrorBar(view, R.string.create_error_cost);
            }
            return null;
        }

        // Extract the data from note, no validation needed
        String note = noteText.getText().toString();

        // Build our subscription object and return it
        return new Subscription(name, cost, date, note);
    }
}
