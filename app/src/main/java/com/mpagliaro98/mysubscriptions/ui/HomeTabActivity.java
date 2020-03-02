package com.mpagliaro98.mysubscriptions.ui;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.tabs.FragmentHome;
import com.mpagliaro98.mysubscriptions.ui.tabs.SectionsPagerAdapter;

/**
 * The activity we'll be on for most of this application's runtime. This holds fragments
 * for each tab and allows navigation between them.
 */
public class HomeTabActivity extends AppCompatActivity {

    public static final String SUBSCRIPTION_MESSAGE = "com.mpagliaro98.mysubscriptions.SUBSCRIPTION";

    private OnDataListenerReceived dataListener;
    private Subscription incomingData;

    public interface OnDataListenerReceived {
        void onDataReceived(Subscription subscription);
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
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        // start on the home tab, the one in the middle
        viewPager.setCurrentItem(1);

        Intent intent = getIntent();
        incomingData = (Subscription)intent.getSerializableExtra(SUBSCRIPTION_MESSAGE);
    }

    public void createButton(View view) {
        Intent intent = new Intent(this, CreateSubscriptionActivity.class);
        startActivity(intent);
    }

    public void setDataListener(OnDataListenerReceived listener) {
        dataListener = listener;
        if (incomingData != null) {
            dataListener.onDataReceived(incomingData);
        }
    }
}