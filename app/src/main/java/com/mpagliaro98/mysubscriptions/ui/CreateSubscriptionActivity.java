package com.mpagliaro98.mysubscriptions.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
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

    /**
     * When this activity is created, initialize it and load any data we need.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Display a different version of this page depending on the parameter passed in
        Intent intent = getIntent();
        PAGE_TYPE pageType = (PAGE_TYPE)intent.getSerializableExtra(PAGE_TYPE_MESSAGE);
        if (pageType == PAGE_TYPE.CREATE) {
            setContentView(R.layout.activity_create_subscription);

            // Auto-fill the date field with the current date, properly formatted
            TextView date = findViewById(R.id.create_date);
            date.setText(new SimpleDateFormat(dateFormat, Locale.getDefault()).format(new Date()));
        } else if (pageType == PAGE_TYPE.VIEW) {
            setContentView(R.layout.activity_create_subscription);

            // Temporary: demonstrates we can get a subscription from the list and view it
            Subscription sub = (Subscription)getIntent().getSerializableExtra(VIEW_SUB_MESSAGE);
            TextView name = findViewById(R.id.create_name);
            name.setText(sub.getName());
        } else if (pageType == PAGE_TYPE.EDIT) {
            setContentView(R.layout.activity_create_subscription);
        }
    }

    /**
     * Called when the create subscription button is pressed. Gets all the data from
     * each input field, creates the Subscription object, then sends it back to the
     * tab activity.
     * @param view the current application view
     */
    public void createSubscription(View view) {
        // Get each input field from the view
        EditText nameText = findViewById(R.id.create_name);
        EditText dateText = findViewById(R.id.create_date);
        EditText costText = findViewById(R.id.create_cost);
        EditText noteText = findViewById(R.id.create_note);

        // Extract the data from each field and verify each is formatted properly
        String name = nameText.getText().toString();
        if (name.equals("")) {
            displayErrorBar(view, R.string.create_error_name);
            return;
        }

        Date date;
        try {
            date = new SimpleDateFormat(dateFormat, Locale.US).parse(dateText.getText().toString());
        } catch (ParseException e) {
            displayErrorBar(view, R.string.create_error_date);
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costText.getText().toString());
        } catch (NumberFormatException e) {
            displayErrorBar(view, R.string.create_error_cost);
            return;
        }

        String note = noteText.getText().toString();

        // Make the object, put it in the intent, and send it to the tab activity
        Subscription subscription = new Subscription(name, cost, date, note);
        Intent intent = new Intent(this, HomeTabActivity.class);
        intent.putExtra(HomeTabActivity.SUBSCRIPTION_MESSAGE, subscription);
        startActivity(intent);
    }

    private void displayErrorBar(View view, int stringId) {
        Snackbar.make(view, stringId, Snackbar.LENGTH_SHORT).show();
    }
}
