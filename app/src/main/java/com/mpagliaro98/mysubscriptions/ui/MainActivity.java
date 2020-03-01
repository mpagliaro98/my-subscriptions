package com.mpagliaro98.mysubscriptions.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.mpagliaro98.mysubscriptions.R;

import java.util.concurrent.TimeUnit;

/**
 * The activity started up upon loading the app, handles initial loading of any data we
 * need prior to starting before going to the tab activity.
 */
public class MainActivity extends AppCompatActivity {

    /**
     * When this activity is created, initialize it, then load any data we need, then
     * pass intent to HomeTabActivity.
     * @param savedInstanceState any saved state needed
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Temporary so we wait on the load-in screen for a little
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Load right into the tab activity
        Intent intent = new Intent(this, HomeTabActivity.class);
        startActivity(intent);
    }
}
