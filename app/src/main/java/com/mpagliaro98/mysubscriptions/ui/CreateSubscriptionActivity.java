package com.mpagliaro98.mysubscriptions.ui;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Currency;
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
        double cost = 0.00; //costText.getText().toString();
        String note = noteText.getText().toString();
        Subscription subscription = new Subscription(name, cost, date, note);
        System.out.println(subscription.getName());
    }
}
