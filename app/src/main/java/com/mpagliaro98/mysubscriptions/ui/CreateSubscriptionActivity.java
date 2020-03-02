package com.mpagliaro98.mysubscriptions.ui;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
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

    /**
     * When this activity is created, initialize it and load any data we need.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_subscription);
    }

    /**
     * Called when the create subscription button is pressed. Gets all the data from
     * each input field, creates the Subscription object, then sends it back to the
     * tab activity.
     * @param view the current application view
     */
    public void createSubscription(View view) {
        // TODO add input validation
        // Get each input field from the view
        EditText nameText = findViewById(R.id.create_name);
        EditText dateText = findViewById(R.id.create_date);
        EditText costText = findViewById(R.id.create_cost);
        EditText noteText = findViewById(R.id.create_note);

        // Extract the data from each field
        String name = nameText.getText().toString();
        Date date;
        try {
            date = new SimpleDateFormat("MM/dd/yyyy", Locale.US).parse(dateText.getText().toString());
        } catch (ParseException e) {
            e.printStackTrace();
            date = null;
        }
        double cost = Double.parseDouble(costText.getText().toString());
        String note = noteText.getText().toString();

        // Make the object, put it in the intent, and send it to the tab activity
        Subscription subscription = new Subscription(name, cost, date, note);
        Intent intent = new Intent(this, HomeTabActivity.class);
        intent.putExtra(HomeTabActivity.SUBSCRIPTION_MESSAGE, subscription);
        startActivity(intent);
    }
}
