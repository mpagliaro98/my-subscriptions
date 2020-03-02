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

public class CreateSubscriptionActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_subscription);
    }

    public void createSubscription(View view) {
        EditText nameText = findViewById(R.id.create_name);
        EditText dateText = findViewById(R.id.create_date);
        EditText costText = findViewById(R.id.create_cost);
        EditText noteText = findViewById(R.id.create_note);
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
        Subscription subscription = new Subscription(name, cost, date, note);
        System.out.println(subscription.getName());

        Intent intent = new Intent(this, HomeTabActivity.class);
        intent.putExtra(HomeTabActivity.SUBSCRIPTION_MESSAGE, subscription);
        startActivity(intent);
    }
}
