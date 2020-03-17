package com.mpagliaro98.mysubscriptions.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.SystemClock;
import android.view.View;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.notifications.AlarmReceiver;
import com.mpagliaro98.mysubscriptions.ui.tabs.FragmentAnalytics;
import com.mpagliaro98.mysubscriptions.ui.tabs.FragmentCalendar;
import com.mpagliaro98.mysubscriptions.ui.tabs.FragmentHome;
import com.mpagliaro98.mysubscriptions.ui.tabs.SectionsPagerAdapter;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * The activity we'll be on for most of this application's runtime. This holds fragments
 * for each tab and allows navigation between them.
 *
 * Data can be passed in here through intents in order to modify data in the underlying
 * model. If a change in data is occurring, any changes require that an INCOMING_TYPE be passed
 * in. If it's CREATE, a Subscription object should also be sent. If it's EDIT, a Subscription
 * object containing changes and the index of that subscription should be sent. If it's DELETE,
 * just the subscription's index should be sent. A saved state bundle is optional in all these
 * cases, but often times it is passed around.
 */
public class MainActivity extends AppCompatActivity {

    // Keys for information that new Subscription objects will have
    public static final String SUBSCRIPTION_MESSAGE = "com.mpagliaro98.mysubscriptions.SUBSCRIPTION";
    public static final String INCOMING_TYPE_MESSAGE = "com.mpagliaro98.mysubscriptions.INCOMING_TYPE";
    public static final String INCOMING_INDEX_MESSAGE = "com.mpagliaro98.mysubscriptions.INCOMING_INDEX";
    // Key for a saved state bundle when returning to a tab
    public static final String SAVED_STATE_BUNDLE_MESSAGE = "com.mpagliaro98.mysubscriptions.SAVED_STATE";

    // The type of action we want to do with the incoming data
    public enum INCOMING_TYPE {CREATE, EDIT, DELETE}

    // Incoming data for handling new or updated Subscription objects
    private Subscription incomingData;
    private INCOMING_TYPE incomingType;
    private Integer incomingIndex;

    /**
     * Fragments under this activity should implement this interface in order to be a
     * receiver of new Subscription objects. onDataReceived will be called in the
     * data listener each time a new Subscription object arrives.
     */
    public interface OnDataListenerReceived {
        void onDataReceived(Subscription subscription, INCOMING_TYPE type, Integer subIndex);
    }

    /**
     * When this activity is created, set-up the SectionsPagerAdapter and build the tab
     * layout, as well as any items that should persist across all fragments.
     * @param savedInstanceState any saved state needed
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Intent intent = getIntent();

        // Create the ViewPager, which handles this activity's child fragments
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        sectionsPagerAdapter.setSavedStateBundle(intent.getBundleExtra(SAVED_STATE_BUNDLE_MESSAGE));
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        // Start on the home tab, the one in the middle
        viewPager.setCurrentItem(1);

        // Check if any data was passed here, and save it to private fields
        incomingData = (Subscription)intent.getSerializableExtra(SUBSCRIPTION_MESSAGE);
        incomingType = (INCOMING_TYPE)intent.getSerializableExtra(INCOMING_TYPE_MESSAGE);
        incomingIndex = intent.getIntExtra(INCOMING_INDEX_MESSAGE, -1);

        // Set the time notifications will be checked
        setRecurringAlarm(getApplicationContext());
    }

    /**
     * Fired when the create button is pressed from any tab. Passes control over
     * to the create subscription activity.
     * @param view the current application view
     */
    public void createButton(View view) {
        Intent intent = new Intent(this, CreateSubscriptionActivity.class);
        intent.putExtra(CreateSubscriptionActivity.PAGE_TYPE_MESSAGE,
                        CreateSubscriptionActivity.PAGE_TYPE.CREATE);
        Bundle savedState = new Bundle();
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        for (Fragment fragment : fragments) {
            if (fragment instanceof FragmentHome) {
                FragmentHome fragmentHome = (FragmentHome)fragment;
                fragmentHome.fillBundleWithSavedState(savedState);
            } else if (fragment instanceof FragmentCalendar) {
                FragmentCalendar fragmentCalendar = (FragmentCalendar)fragment;
            } else if (fragment instanceof FragmentAnalytics) {
                FragmentAnalytics fragmentAnalytics = (FragmentAnalytics)fragment;
            }
        }
        intent.putExtra(SAVED_STATE_BUNDLE_MESSAGE, savedState);
        startActivity(intent);
    }

    /**
     * Allows a class implementing OnDataListenerReceived to set themselves as the receiver
     * of incoming Subscription objects. This should be called from a child fragment. If
     * any incoming data exists, the listener will be called to handle it.
     * @param listener the class that should handle new incoming objects
     */
    public void checkIncomingData(OnDataListenerReceived listener) {
        if (incomingType != null) {
            listener.onDataReceived(incomingData, incomingType, incomingIndex);
        }
    }

    /**
     * Set up the alarm, which at a certain time each day will run the notification service.
     * @param context the context to create the intent with
     */
    public static void setRecurringAlarm(Context context) {
        // Get how long it will be from now until the time notifications will go off
        Calendar alarmTime = Calendar.getInstance();
        Date now = alarmTime.getTime();
        alarmTime.set(Calendar.HOUR_OF_DAY, 6);
        alarmTime.set(Calendar.MINUTE, 0);
        alarmTime.set(Calendar.SECOND, 0);
        alarmTime.set(Calendar.MILLISECOND, 0);
        if (now.after(alarmTime.getTime())) {
            alarmTime.add(Calendar.DATE, 1);
        }
        long timeUntilAlarm = alarmTime.getTime().getTime() - now.getTime();

        // Build the pending intent and set the alarm
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(AlarmReceiver.intentAction);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
                0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarmManager = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        assert alarmManager != null;
        alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME,
                SystemClock.elapsedRealtime() + timeUntilAlarm, AlarmManager.INTERVAL_DAY, pendingIntent);
    }
}