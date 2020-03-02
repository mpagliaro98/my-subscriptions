package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.content.Context;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import com.mpagliaro98.mysubscriptions.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    // The array containing the names of each tab
    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_calendar, R.string.tab_text_home, R.string.tab_text_analytics};
    private final Context mContext;

    /**
     * Create the pager adapter.
     * @param context the current context of the application
     * @param fm the parent fragment manager
     */
    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    /**
     * Get a fragment for the given tab position.
     * @param position the tab that is being requested
     * @return Calendar for position 0, Home for 1, Analytics for 2, or null otherwise
     */
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new FragmentCalendar();
            case 1:
                return new FragmentHome();
            case 2:
                return new FragmentAnalytics();
            default:
                return null;
        }
    }

    /**
     * Get the page title for a given tab.
     * @param position the tab that is being requested
     * @return that tab's title as listed in the tab titles array
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    /**
     * Get the total number of tabs.
     * @return the number of tabs
     */
    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}