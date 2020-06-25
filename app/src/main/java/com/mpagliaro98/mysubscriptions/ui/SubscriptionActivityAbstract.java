package com.mpagliaro98.mysubscriptions.ui;

import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Category;
import com.mpagliaro98.mysubscriptions.model.SettingsManager;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.model.ZeroTimeCalendar;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * The base class for any activities that are used to display subscription data. For this
 * application, this is the create, edit, and view activities. They all use the same view
 * layout, but modify it slightly in their distinct subclasses based on what each view
 * mode requires. To access any of these activities from an external activity, each subclass
 * of this class should have a static helper method that builds an intent with the
 * necessary information that page needs.
 */
public abstract class SubscriptionActivityAbstract extends AppCompatActivity {

    // Messages to be passed through intents to this activity
    public static final String VIEW_SUB_MESSAGE = "com.mpagliaro98.mysubscriptions.VIEW_SUB";
    public static final String SUB_ID_MESSAGE = "com.mpagliaro98.mysubscriptions.SUB_ID";

    // The subscription object being used by this page
    protected Subscription sub;
    // The index of the subscription we are currently looking at
    protected int subIndex;
    // The saved state bundle from the previous activity
    protected Bundle savedState;

    // List of every valid category, used to populate input fields
    protected ArrayList<Category> categoryList;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * When this activity is created, initialize it and load any data we need. This will do
     * global initializations, then pass on control to the subclass to make any additional
     * customizations.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        // Put the back button on this activity's title bar
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Set the settings-defined currency symbol in the cost text field and date format in date field
        TextView costText = findViewById(R.id.create_text_cost);
        TextView dateText = findViewById(R.id.create_date);
        String currencySymbol;
        String dateFormat;
        try {
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            currencySymbol = settingsManager.getCurrencySymbol();
            dateFormat = settingsManager.getDateFormat();
        } catch (IOException e) {
            currencySymbol = getString(R.string.currency_default);
            dateFormat = getString(R.string.date_format_default);
        }
        String costTextStr = costText.getText() + currencySymbol +
                getString(R.string.create_text_cost2);
        costText.setText(costTextStr);
        dateText.setHint(dateFormat);

        // Initialize the UI for the category dropdown and color
        initializeCategories();
        initializeCategoryUI();

        // Initialize the UI for the date picker
        initializeDatePickerUI();

        // Process incoming data through the intent
        Intent intent = getIntent();
        // This incoming subscription will be null when page type is CREATE
        sub = (Subscription)intent.getSerializableExtra(VIEW_SUB_MESSAGE);
        // Save the index of this subscription, if it's null it isn't needed and will be set to -1
        subIndex = intent.getIntExtra(SUB_ID_MESSAGE, -1);
        // Saved the state from the previous activity so we can send it back when we return
        savedState = intent.getBundleExtra(MainActivity.SAVED_STATE_BUNDLE_MESSAGE);

        // Apply changes to the UI based on this activity's subclass
        onCreateSubclass();
    }

    /**
     * Creates the action bar menu at the top of the page. As the default implementation, this
     * makes no changes to the menu bar.
     * @param menu the menu to inflate
     * @return whether creation was successful or not
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_create_subscription, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called when a button on the action bar is pressed. When the edit button is pressed
     * in this activity, the edit activity will be launched, sending the subscription
     * currently being viewed through the intent. When the delete button is pressed, display
     * a confirmation dialog, and if yes is chosen, return to the main tab page and delete
     * the selected item. This is always the same across all versions of this activity.
     * @param item the item on the action bar that was pressed
     * @return whether it completed successfully or not
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        // When the edit button is pressed, reload this page in edit mode
        if (id == R.id.create_edit_button) {
            Intent intent = EditSubscriptionActivity.buildGeneralEditIntent(this,
                    parseInputFields(null), subIndex, savedState);
            startActivity(intent);
        }
        // When the delete button is pressed, display a yes/no dialog
        else if (id == R.id.create_delete_button) {
            final SubscriptionActivityAbstract packageContext = this;
            new AlertDialog.Builder(this)
                    .setTitle(getString(R.string.create_delete_dialog_title))
                    .setMessage(getString(R.string.create_delete_dialog_content))
                    .setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = MainActivity.buildGeneralMainIntent(packageContext,
                                    MainActivity.INCOMING_TYPE.DELETE, null, subIndex, savedState);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton(R.string.no, null).show();
        }
        // Otherwise, treat the action as using the back button
        else {
            backButton();
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Overrides the functionality of the Android back button when on a subscription page.
     * This calls the subclass, so each subclass must specify what to do when the back
     * button is pressed.
     */
    @Override
    public void onBackPressed() {
        backButton();
    }

    /**
     * Called when the create/update subscription button is pressed. By default, this will
     * display an error that the button shouldn't be accessible. Subclasses can override this
     * method to provide functionality for the submit button.
     * @param view the current application view
     */
    public void submitSubscription(View view) {
        displayErrorBar(findViewById(android.R.id.content), R.string.create_error_no_button);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // ABSTRACT METHODS //////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Abstract method for what a subclass should do when the activity is created. This is
     * always called after onCreate is run and the global activity properties are initialized.
     */
    public abstract void onCreateSubclass();

    /**
     * Abstract method for providing functionality to the back button. This is called either
     * when pressing the back button in the top left, or using the system back button.
     */
    public abstract void backButton();

    //////////////////////////////////////////////////////////////////////////////////////////
    // PROTECTED METHODS /////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Parse the input fields in the current view and build a subscription object from
     * them. This will display an error message and return null if one of the fields is
     * invalid.
     * @param view the current application view, if null then error bars won't display
     * @return a Subscription object if everything is valid, null otherwise
     */
    protected Subscription parseInputFields(View view) {
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
        if (name.length() > 50) {
            displayErrorBar(view, R.string.create_error_name_length);
            return null;
        }

        // Extract the data from date and validate it. The hour, minute, second, and millisecond
        // of this date field will all be 0. (an assumption needed by the Subscription)
        Date date;
        String dateFormat;
        try {
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            dateFormat = settingsManager.getDateFormat();
        } catch (IOException e) {
            dateFormat = getString(R.string.date_format_default);
        }
        if (dateText.getText().toString().length() > 10) {
            displayErrorBar(view, R.string.create_error_date_length);
            return null;
        }
        try {
            date = new SimpleDateFormat(dateFormat, Locale.US).parse(dateText.getText().toString());
        } catch (ParseException e) {
            displayErrorBar(view, R.string.create_error_date);
            return null;
        }

        // Extract the data from cost and validate it, remove the currency symbol if it's there
        double cost;
        String costTemp = costText.getText().toString();
        String currencySymbol;
        try {
            SettingsManager settingsManager = new SettingsManager(getApplicationContext());
            currencySymbol = settingsManager.getCurrencySymbol();
        } catch (IOException e) {
            currencySymbol = getString(R.string.currency_default);
        }
        if (costTemp.startsWith(currencySymbol)) {
            costTemp = costTemp.substring(currencySymbol.length());
        }
        if (costTemp.length() > 15) {
            displayErrorBar(view, R.string.create_error_cost_length);
            return null;
        }
        try {
            cost = Double.parseDouble(costTemp);
            if (cost < 0.01) {
                displayErrorBar(view, R.string.create_error_cost_zero);
                return null;
            }
        } catch (NumberFormatException e) {
            displayErrorBar(view, R.string.create_error_cost);
            return null;
        }

        // Extract the data from note, check the length matches the limit in the view
        String note = noteText.getText().toString();
        if (note.length() > 2000) {
            displayErrorBar(view, R.string.create_error_note_length);
            return null;
        }

        // Set the number of months based on the selection, display an error if an invalid value is found
        int freqMonths;
        if (freqDropdown.getSelectedItem().equals(getString(R.string.array_freq_monthly))) {
            freqMonths = 1;
        } else if (freqDropdown.getSelectedItem().equals(getString(R.string.array_freq_bimonthly))) {
            freqMonths = 2;
        } else if (freqDropdown.getSelectedItem().equals(getString(R.string.array_freq_trimonthly))) {
            freqMonths = 3;
        } else if (freqDropdown.getSelectedItem().equals(getString(R.string.array_freq_twiceyear))) {
            freqMonths = 6;
        } else if (freqDropdown.getSelectedItem().equals(getString(R.string.array_freq_yearly))) {
            freqMonths = 12;
        } else {
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
        return new Subscription(-1, name, cost, date, note, freqMonths, category, notifDays);
    }

    /**
     * Get what position the notification dropdown should be in based on what that
     * field is set to in the Subscription.
     * @param notifDays the number of days before the notification should go off
     * @return the position the dropdown should be in, default is 0
     */
    protected int getNotifDropdownSelection(int notifDays) {
        if (notifDays == 0) {
            return 1;
        } else if (notifDays == 1) {
            return 2;
        } else if (notifDays == 2) {
            return 3;
        } else if (notifDays == 3) {
            return 4;
        } else if (notifDays == 7) {
            return 5;
        } else {
            return 0;
        }
    }

    /**
     * Get what position the recharge frequency dropdown should be in based on what that
     * field is set to in the Subscription.
     * @param rechargeFreq the number of months between charges
     * @return the position the dropdown should be in, default is 0
     */
    protected int getRechargeDropdownSelection(int rechargeFreq) {
        if (rechargeFreq == 1) {
            return 0;
        } else if (rechargeFreq == 2) {
            return 1;
        } else if (rechargeFreq == 3) {
            return 2;
        } else if (rechargeFreq == 6) {
            return 3;
        } else if (rechargeFreq == 12) {
            return 4;
        } else {
            return 0;
        }
    }

    /**
     * Get what position the category dropdown should be in based on what that
     * field is set to in the Subscription.
     * @param category the object representing the category of the subscription
     * @return the position the dropdown should be in, default is 0
     */
    protected int getCategoryDropdownSelection(Category category) {
        for (int catIndex = 0; catIndex < categoryList.size(); catIndex++) {
            if (category.equals(categoryList.get(catIndex))) {
                return catIndex;
            }
        }
        return 0;
    }

    /**
     * Display a small bar on the bottom of the screen. Should be called when there is
     * something to display regarding a recent action (like improperly formatted input
     * when submitting something). If view is null, nothing will display.
     * @param view the current view of the application
     * @param stringId the string to display, should be in strings.xml
     */
    protected void displayErrorBar(View view, int stringId) {
        if (view != null) {
            Snackbar.make(view, stringId, Snackbar.LENGTH_SHORT).show();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create each valid category object and add them to the category list. This is initialized
     * here and not at the category list declaration so that we can call getColor().
     */
    private void initializeCategories() {
        categoryList = new ArrayList<Category>() {
            {
                add(new Category(getResources().getColor(R.color.colorCategoryVideoStreaming), "Video Streaming"));
                add(new Category(getResources().getColor(R.color.colorCategoryAudioStreaming), "Audio Streaming"));
                add(new Category(getResources().getColor(R.color.colorCategoryGaming), "Gaming"));
                add(new Category(getResources().getColor(R.color.colorCategoryShopping), "Online Shopping"));
                add(new Category(getResources().getColor(R.color.colorCategoryMisc), "Misc"));
            }};
    }

    /**
     * A helper function to initialize parts of the UI concerning category selection. This will
     * populate the dropdown list with each valid category, then create the listener that will
     * change the category color depending on the selected category.
     */
    private void initializeCategoryUI() {
        final ImageView catColor = findViewById(R.id.create_category_color);
        final Spinner category = findViewById(R.id.create_category_dropdown);

        // Set the list of items in the category dropdown
        ArrayAdapter<Category> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, categoryList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        category.setAdapter(adapter);

        // Create a listener that sets the category's color beside the dropdown
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                catColor.setColorFilter(((Category)category.getSelectedItem()).getColor(),
                        PorterDuff.Mode.SRC_IN);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * A helper function to initialize parts of the UI concerning the date picker. This will
     * create the listener for the date picker, which causes a date selection to appear.
     */
    private void initializeDatePickerUI() {
        final TextView date = findViewById(R.id.create_date);
        ImageView datePicker = findViewById(R.id.create_date_picker);

        // Create the functionality for the date picker
        datePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ZeroTimeCalendar zeroTimeCalendar = new ZeroTimeCalendar();
                String dateFormat;
                try {
                    SettingsManager settingsManager = new SettingsManager(getApplicationContext());
                    dateFormat = settingsManager.getDateFormat();
                } catch (IOException e) {
                    dateFormat = getString(R.string.date_format_default);
                }
                try {
                    Date startDate = new SimpleDateFormat(dateFormat, Locale.US).parse(date.getText().toString());
                    assert startDate != null;
                    zeroTimeCalendar.setTimeToDate(startDate);
                } catch (ParseException e) {
                    zeroTimeCalendar.setTimeToDate(new Date());
                }
                int day = zeroTimeCalendar.getDayOfMonth();
                int month = zeroTimeCalendar.getMonth();
                int year = zeroTimeCalendar.getYear();
                // Start the date picker display at the date in the start date field if valid, if
                // not it displays at today's date
                DatePickerDialog picker = new DatePickerDialog(SubscriptionActivityAbstract.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                ZeroTimeCalendar zeroTimeCalendar = new ZeroTimeCalendar();
                                zeroTimeCalendar.setTime(year, monthOfYear, dayOfMonth);
                                Date enteredDate = zeroTimeCalendar.getCurrentDate();
                                String dateFormat;
                                try {
                                    SettingsManager settingsManager = new SettingsManager(getApplicationContext());
                                    dateFormat = settingsManager.getDateFormat();
                                } catch (IOException e) {
                                    dateFormat = getString(R.string.date_format_default);
                                }
                                date.setText(new SimpleDateFormat(dateFormat, Locale.US).format(enteredDate));
                            }
                        }, year, month, day);
                picker.show();
            }
        });
    }
}
