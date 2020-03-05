package com.mpagliaro98.mysubscriptions.ui;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.tabs.SectionsPagerAdapter;

/**
 * The activity we'll be on for most of this application's runtime. This holds fragments
 * for each tab and allows navigation between them.
 */
public class HomeTabActivity extends AppCompatActivity {

    // The key that new Subscription objects will be under in an incoming intent
    public static final String SUBSCRIPTION_MESSAGE = "com.mpagliaro98.mysubscriptions.SUBSCRIPTION";
    public static final String INCOMING_TYPE_MESSAGE = "com.mpagliaro98.mysubscriptions.INCOMING_TYPE";
    public static final String INCOMING_INDEX_MESSAGE = "com.mpagliaro98.mysubscriptions.INCOMING_INDEX";

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
        setContentView(R.layout.activity_home_tab);

        // Create the ViewPager, which handles this activity's child fragments
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        // Start on the home tab, the one in the middle
        viewPager.setCurrentItem(1);

        // Check if any data was passed here, and save it to private fields
        Intent intent = getIntent();
        incomingData = (Subscription)intent.getSerializableExtra(SUBSCRIPTION_MESSAGE);
        incomingType = (INCOMING_TYPE)intent.getSerializableExtra(INCOMING_TYPE_MESSAGE);
        incomingIndex = intent.getIntExtra(INCOMING_INDEX_MESSAGE, -1);
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
}