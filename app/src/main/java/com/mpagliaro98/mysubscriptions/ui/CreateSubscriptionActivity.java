package com.mpagliaro98.mysubscriptions.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.media.Image;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.model.Category;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * An activity for gathering all the data on a new subscription to create, compiling
 * all the data into a subscription object, then sending it back to the tab activity
 * so it can be added to the model.
 *
 * Data can be passed in here through intents in order to change how the page displays
 * data. A PAGE_TYPE must always be sent to specify the type of page. If it's CREATE,
 * no additional data needs to be sent. If it's EDIT or VIEW, a Subscription object to
 * view or modify must be passed in, as well as its index in the underlying model (this
 * index acts like a unique ID).
 */
public class CreateSubscriptionActivity extends AppCompatActivity {

    private static final String dateFormat = "MM/dd/yyyy";
    public static final String PAGE_TYPE_MESSAGE = "com.mpagliaro98.mysubscriptions.PAGE_TYPE";
    public static final String VIEW_SUB_MESSAGE = "com.mpagliaro98.mysubscriptions.VIEW_SUB";
    public static final String SUB_ID_MESSAGE = "com.mpagliaro98.mysubscriptions.SUB_ID";

    // Different versions this page can be, send this to this page each time it is accessed
    public enum PAGE_TYPE {CREATE, EDIT, VIEW};

    // The current state of this page
    private PAGE_TYPE pageType;
    // The index of the subscription we are currently looking at
    private int subIndex;

    // List of every valid category, used to populate input fields
    private ArrayList<Category> categoryList = new ArrayList<Category>() {
        {
            add(new Category(R.color.colorCategoryStreaming, "Streaming"));
            add(new Category(R.color.colorCategoryGaming, "Gaming"));
            add(new Category(R.color.colorCategoryShopping, "Online Shopping"));
        }};

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
        Spinner frequency = findViewById(R.id.create_freq_dropdown);
        TextView nextDate = findViewById(R.id.create_next_date);
        Button createButton = findViewById(R.id.create_button_finish);
        final ImageView catColor = findViewById(R.id.create_category_color);
        final Spinner category = findViewById(R.id.create_category_dropdown);
        Spinner notifications = findViewById(R.id.create_notif_dropdown);

        // Set the list of items in the category dropdown
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);

        // Create a listener that sets the category's color beside the dropdown
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catColor.setColorFilter(getResources().getColor(((Category)category.getSelectedItem()).getColor()),
                        PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        Intent intent = getIntent();
        // This incoming subscription will be null when page type is CREATE
        Subscription sub = (Subscription)intent.getSerializableExtra(VIEW_SUB_MESSAGE);
        // Display a different version of this page depending on the parameter passed in
        pageType = (PAGE_TYPE)intent.getSerializableExtra(PAGE_TYPE_MESSAGE);
        // Save the index of this subscription, if it's null it isn't needed and will be set to -1
        subIndex = intent.getIntExtra(SUB_ID_MESSAGE, -1);

        // For create, set the page to the create version with editable, empty fields
        if (pageType == PAGE_TYPE.CREATE) {
            // Auto-fill the date field with the current date, properly formatted
            date.setText(new SimpleDateFormat(dateFormat, Locale.getDefault()).format(new Date()));
            // Make sure the next date field can't be seen when creating
            nextDate.setVisibility(View.INVISIBLE);
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
            // Set the dropdown to this subscription's value
            frequency.setEnabled(false);
            String[] dropdownStrings = getResources().getStringArray(R.array.array_freq_dropdown);
            for (int strIndex = 0; strIndex < dropdownStrings.length; strIndex++) {
                if (sub.getRechargeFrequency().equals(dropdownStrings[strIndex])) {
                    frequency.setSelection(strIndex);
                }
            }
            // Write the next payment date to the screen
            String nextDateStr = "Next Payment Date: " +
                    new SimpleDateFormat(dateFormat, Locale.US).format(sub.getNextPaymentDate());
            nextDate.setText(nextDateStr);
            // Set the category dropdown to this subscription's category
            category.setEnabled(false);
            for (int catIndex = 0; catIndex < categoryList.size(); catIndex++) {
                if (sub.getCategory().equals(categoryList.get(catIndex))) {
                    category.setSelection(catIndex);
                }
            }
            // Set the notifications dropdown to the proper value
            notifications.setEnabled(false);
            if (sub.getNotifDays() == 0) {
                notifications.setSelection(1);
            } else if (sub.getNotifDays() == 1) {
                notifications.setSelection(2);
            } else if (sub.getNotifDays() == 2) {
                notifications.setSelection(3);
            } else if (sub.getNotifDays() == 3) {
                notifications.setSelection(4);
            } else if (sub.getNotifDays() == 7) {
                notifications.setSelection(5);
            }
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
            // Set the dropdown to this subscription's value
            String[] dropdownStrings = getResources().getStringArray(R.array.array_freq_dropdown);
            for (int strIndex = 0; strIndex < dropdownStrings.length; strIndex++) {
                if (sub.getRechargeFrequency().equals(dropdownStrings[strIndex])) {
                    frequency.setSelection(strIndex);
                }
            }
            // Set the category dropdown to this subscription's category
            for (int catIndex = 0; catIndex < categoryList.size(); catIndex++) {
                if (sub.getCategory().equals(categoryList.get(catIndex))) {
                    category.setSelection(catIndex);
                }
            }
            // Set the notifications dropdown to the proper value
            if (sub.getNotifDays() == 0) {
                notifications.setSelection(1);
            } else if (sub.getNotifDays() == 1) {
                notifications.setSelection(2);
            } else if (sub.getNotifDays() == 2) {
                notifications.setSelection(3);
            } else if (sub.getNotifDays() == 3) {
                notifications.setSelection(4);
            } else if (sub.getNotifDays() == 7) {
                notifications.setSelection(5);
            }
            // Make sure the next date field can't be seen when editing
            nextDate.setVisibility(View.INVISIBLE);
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
            MenuItem editItem = menu.findItem(R.id.create_edit_button);
            editItem.setVisible(false);
        }
        if (pageType == PAGE_TYPE.CREATE) {
            MenuItem deleteItem = menu.findItem(R.id.create_delete_button);
            deleteItem.setVisible(false);
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
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.SUBSCRIPTION_MESSAGE, subscription);
            intent.putExtra(MainActivity.INCOMING_TYPE_MESSAGE,
                            MainActivity.INCOMING_TYPE.CREATE);
            startActivity(intent);
        }
        else if (pageType == PAGE_TYPE.EDIT) {
            // Get a subscription from the data in all the input fields, halt if data is invalid
            Subscription subscription = parseInputFields(view);
            if (subscription == null) {
                return;
            }

            // Put the object and its index in the intent and send it to the tab activity
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra(MainActivity.SUBSCRIPTION_MESSAGE, subscription);
            intent.putExtra(MainActivity.INCOMING_TYPE_MESSAGE,
                            MainActivity.INCOMING_TYPE.EDIT);
            intent.putExtra(MainActivity.INCOMING_INDEX_MESSAGE,
                            subIndex);
            startActivity(intent);
        }
    }

    /**
     * Called when a button on the action bar is pressed. When the edit button is pressed
     * in this activity, we will reload the page in edit mode, sending the subscription
     * currently being viewed through the intent. When the delete button is pressed, display
     * a confirmation dialog, and if yes is chosen, return to the main tab page and delete
     * the selected item.
     * @param item the item on the action bar that was pressed
     * @return whether it completed successfully or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the edit button is pressed, reload this page in edit mode
        if (id == R.id.create_edit_button) {
            Intent intent = new Intent(this, CreateSubscriptionActivity.class);
            intent.putExtra(CreateSubscriptionActivity.PAGE_TYPE_MESSAGE,
                            PAGE_TYPE.EDIT);
            Subscription subscription = parseInputFields(null);
            intent.putExtra(CreateSubscriptionActivity.VIEW_SUB_MESSAGE,
                            subscription);
            intent.putExtra(CreateSubscriptionActivity.SUB_ID_MESSAGE,
                            subIndex);
            startActivity(intent);
        }
        // When the delete button is pressed, display a yes/no dialog
        else if (id == R.id.create_delete_button) {
            final CreateSubscriptionActivity packageContext = this;
            new AlertDialog.Builder(this)
                    .setTitle("Delete")
                    .setMessage("Would you like to delete this Subscription?")
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent(packageContext, MainActivity.class);
                            intent.putExtra(MainActivity.INCOMING_TYPE_MESSAGE,
                                    MainActivity.INCOMING_TYPE.DELETE);
                            intent.putExtra(MainActivity.INCOMING_INDEX_MESSAGE,
                                    subIndex);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.no, null).show();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Display a small bar on the bottom of the screen. Should be called when there is
     * something to display regarding a recent action (like improperly formatted input
     * when submitting something). If view is null, nothing will display.
     * @param view the current view of the application
     * @param stringId the string to display, should be in strings.xml
     */
    private void displayErrorBar(View view, int stringId) {
        if (view != null) {
            Snackbar.make(view, stringId, Snackbar.LENGTH_SHORT).show();
        }
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
        Spinner freqDropdown = findViewById(R.id.create_freq_dropdown);

        // Extract the data from name and validate it
        String name = nameText.getText().toString();
        if (name.equals("")) {
            displayErrorBar(view, R.string.create_error_name);
            return null;
        }

        // Extract the data from date and validate it
        Date date;
        try {
            date = new SimpleDateFormat(dateFormat, Locale.US).parse(dateText.getText().toString());
        } catch (ParseException e) {
            displayErrorBar(view, R.string.create_error_date);
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
            displayErrorBar(view, R.string.create_error_cost);
            return null;
        }

        // Extract the data from note, no validation needed
        String note = noteText.getText().toString();

        // Extract the string from the dropdown, make sure it's a valid string from the list
        String rechargeFrequency = freqDropdown.getSelectedItem().toString();
        String[] dropdownStrings = getResources().getStringArray(R.array.array_freq_dropdown);
        boolean errorInDropdown = true;
        for (String dropdownStr : dropdownStrings) {
            if (rechargeFrequency.equals(dropdownStr)) {
                errorInDropdown = false;
                break;
            }
        }
        if (errorInDropdown) {
            displayErrorBar(view, R.string.create_error_frequency);
            return null;
        }

        // Set the selected category as this subscription's category
        Spinner categoryDropdown = findViewById(R.id.create_category_dropdown);
        Category category = (Category)categoryDropdown.getSelectedItem();

        // Set the number of days before the next payment date the notification is triggered
        Spinner notifDropdown = findViewById(R.id.create_notif_dropdown);
        int notifDays = -1;
        if (notifDropdown.getSelectedItem().equals(getString(R.string.array_notif_day_of))) {
            notifDays = 0;
        } else if (notifDropdown.getSelectedItem().equals(getString(R.string.array_notif_day_before))) {
            notifDays = 1;
        } else if (notifDropdown.getSelectedItem().equals(getString(R.string.array_notif_two_days))) {
            notifDays = 2;
        } else if (notifDropdown.getSelectedItem().equals(getString(R.string.array_notif_three_days))) {
            notifDays = 3;
        } else if (notifDropdown.getSelectedItem().equals(getString(R.string.array_notif_week))) {
            notifDays = 7;
        }

        // Build our subscription object and return it, set the unique ID as -1 as we will
        // give it its proper value in the model
        return new Subscription(-1, name, cost, date, note, rechargeFrequency, category, notifDays, getResources());
    }
}
