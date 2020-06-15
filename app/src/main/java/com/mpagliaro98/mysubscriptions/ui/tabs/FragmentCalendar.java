package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.model.ZeroTimeCalendar;
import com.mpagliaro98.mysubscriptions.ui.CreateSubscriptionActivity;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionCalendar;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionView;
import com.mpagliaro98.mysubscriptions.ui.interfaces.CalendarEventHandler;
import com.mpagliaro98.mysubscriptions.ui.interfaces.OnSyncCalendarListener;
import com.mpagliaro98.mysubscriptions.ui.interfaces.SavedStateCompatible;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * A fragment containing the view for the calendar tab.
 */
public class FragmentCalendar extends Fragment implements SavedStateCompatible, OnSyncCalendarListener {

    // The model shared by the three main tabs
    private SharedViewModel model;
    // Any saved state from previously in the application to apply when loading the view
    private Bundle savedState;

    // Keys for the saved state of the calendar fragment when returning
    public static final String SAVED_STATE_MONTH_MESSAGE = "com.mpagliaro98.mysubscriptions.C_SAVED_MONTH";
    public static final String SAVED_STATE_SELECTED_DATE_MESSAGE = "com.mpagliaro98.mysubscriptions.C_SAVED_SELECTED_DATE";
    public static final String SAVED_STATE_SCROLL_MESSAGE = "com.mpagliaro98.mysubscriptions.C_SAVED_SCROLL";

    // The account type used to create the sync calendar
    private static final String CALENDAR_ACCOUNT_TYPE = "com.mpagliaro98";
    // Projection array. Creating indices for this array instead of doing dynamic lookups improves performance.
    private static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };
    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;

    // The request code used when asking for permission to access the calendar
    public static final int PERMISSION_CALENDAR_REQUEST_CODE = 100;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create this fragment object and set its saved state bundle to be used later.
     * @param savedState bundle of saved state
     */
    public FragmentCalendar(Bundle savedState) {
        super();
        this.savedState = savedState;
    }

    /**
     * Default empty constructor for this fragment.
     */
    public FragmentCalendar() {}

    /**
     * Initializes the model for the calendar tab.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MainActivity mainActivity = (MainActivity)getActivity();
        assert mainActivity != null;
        model = new ViewModelProvider(mainActivity).get(SharedViewModel.class);

        // Set this fragment as the listener for the sync calendar button
        mainActivity.setSyncCalendarListener(this);
    }

    /**
     * Creates the root view for this fragment.
     * @param inflater inflater to instantiate the xml view into an object
     * @param container the group that will serve as the base for the view
     * @param savedInstanceState any saved state needed
     * @return the view that displays this fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_calendar_tab, container, false);

        // Gather up all next payment dates for all subscriptions
        List<Subscription> subList = model.getFullSubscriptionList();
        HashSet<Date> nextPaymentDates = new HashSet<>();
        for (Subscription sub : subList) {
            if (sub.getNextPaymentList() != null) {
                nextPaymentDates.addAll(sub.getNextPaymentList());
            }
        }

        // Update the calendar using the set of next payment dates as the events
        SubscriptionCalendar subCalendar = root.findViewById(R.id.subscription_calendar);
        subCalendar.setEvents(nextPaymentDates);
        subCalendar.updateCalendar();

        // Set a listener for the calendar
        subCalendar.setCalendarEventHandler(new CalendarEventHandler() {
            @Override
            public void onDayPress(Date date) {
                updateCalendarTabOnDayPress(date, root);
                SubscriptionCalendar subCalendar = root.findViewById(R.id.subscription_calendar);
                subCalendar.setSelectedDate(date);
            }
        });

        // Set the calendar to default to today's date when first loaded
        Date currentDate = new ZeroTimeCalendar().getCurrentDate();
        updateCalendarTabOnDayPress(currentDate, root);
        subCalendar.setSelectedDate(currentDate);

        // Apply the values from the saved state to the page
        if (savedState != null) {
            applySavedState(savedState, root);
        }
        return root;
    }

    /**
     * Populate a given bundle with values pertaining to how this fragment is set.
     * @param bundle the bundle to place the saved items in
     */
    @Override
    public void fillBundleWithSavedState(Bundle bundle) {
        View view = getView();
        assert view != null;
        SubscriptionCalendar subCalendar = view.findViewById(R.id.subscription_calendar);
        bundle.putSerializable(SAVED_STATE_MONTH_MESSAGE, subCalendar.getDisplayedMonth());
        Date selectedDate = subCalendar.getSelectedDate();
        bundle.putSerializable(SAVED_STATE_SELECTED_DATE_MESSAGE, selectedDate);
        ScrollView scrollView = view.findViewById(R.id.calendar_scroll_view);
        bundle.putInt(SAVED_STATE_SCROLL_MESSAGE, scrollView.getScrollY());
    }

    /**
     * Given a bundle of saved state, extract the values that were saved to it previously
     * and re-apply them to this view.
     * @param savedState bundle of saved state, must not be null
     * @param root the root view of this tab
     */
    @Override
    public void applySavedState(@NonNull final Bundle savedState, View root) {
        if (savedState.containsKey(SAVED_STATE_MONTH_MESSAGE)) {
            SubscriptionCalendar subCalendar = root.findViewById(R.id.subscription_calendar);
            Date date = (Date)savedState.getSerializable(SAVED_STATE_MONTH_MESSAGE);
            assert date != null;
            subCalendar.setCalendarToMonth(date);
        }
        if (savedState.containsKey(SAVED_STATE_SELECTED_DATE_MESSAGE)) {
            SubscriptionCalendar subCalendar = root.findViewById(R.id.subscription_calendar);
            Date selectedDate = (Date)savedState.getSerializable(SAVED_STATE_SELECTED_DATE_MESSAGE);
            assert selectedDate != null;
            subCalendar.setSelectedDate(selectedDate);
            updateCalendarTabOnDayPress(selectedDate, root);
        }
        if (savedState.containsKey(SAVED_STATE_SCROLL_MESSAGE)) {
            final ScrollView scrollView = root.findViewById(R.id.calendar_scroll_view);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, savedState.getInt(SAVED_STATE_SCROLL_MESSAGE));
                }
            });
        }
    }

    /**
     * Called when the button to sync the calendar is pressed. This will assert that the app
     * has permission to use the calendar API, then create the calendar on the system and
     * create events on the calendar for each subscription's payment dates.
     */
    @Override
    public void syncCalendar() {
        Activity parentActivity = getActivity();
        assert parentActivity != null;
        Context context = getContext();
        assert context != null;
        View parentView = getView();
        assert parentView != null;

        // If we don't have both read and write permissions for the calendar, request them
        // Don't continue unless we have both permissions
        if (ActivityCompat.checkSelfPermission(parentActivity, Manifest.permission.READ_CALENDAR)
                != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                        parentActivity, Manifest.permission.WRITE_CALENDAR)
                != PackageManager.PERMISSION_GRANTED) {
            requestCalendarPermissions(parentActivity);
            return;
        }

        // Delete the existing version of this calendar on the system and recreate it
        deleteSyncCalendar(context);
        createSyncCalendar(context, getString(R.string.calendar_sync_name));

        // Create the query to find the created calendar and its ID
        ContentResolver cr = context.getContentResolver();
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?) AND ("
                + CalendarContract.Calendars.OWNER_ACCOUNT + " = ?))";
        String[] selectionArgs = new String[] {getString(R.string.app_name), CALENDAR_ACCOUNT_TYPE,
                getString(R.string.app_name)};

        // Submit the query and get a Cursor object back
        Cursor cur = cr.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);

        // Use the cursor to step through the returned records (should just be the one calendar)
        assert cur != null;
        while (cur.moveToNext()) {
            // Get the calendar ID
            int calID = (int)cur.getLong(PROJECTION_ID_INDEX);

            // Loop through each subscription and create events for each of their payment dates
            for (Subscription sub : model.getFullSubscriptionList()) {
                createSyncCalendarEvents(context, calID, sub);
            }
        }
        cur.close();

        // Display a success message at the end
        Snackbar.make(parentView, R.string.calendar_sync_success, Snackbar.LENGTH_LONG).show();
    }

    /**
     * Handles the outcome of requesting a permission. If the calendar permissions are requested,
     * the overridden method in the parent activity will call this to handle it. This method
     * checks to see if both permissions are granted, and calls the sync calendar method if they
     * are; otherwise, it displays a message saying permission is denied.
     * @param permissions an array of permissions that were requested
     * @param grantResults an array of integers showing the results of those permission requests
     */
    @Override
    public void handleRequestResult(@NonNull String[] permissions, @NonNull int[] grantResults) {
        View view = getView();
        assert view != null;
        if (grantResults.length == 2 && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
            Snackbar.make(view, R.string.calendar_sync_permissions_granted, Snackbar.LENGTH_LONG).show();
            syncCalendar();
        } else {
            Snackbar.make(view, R.string.calendar_sync_permissions_denied, Snackbar.LENGTH_LONG).show();
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Update the UI component that displays a list of subscriptions depending on what day
     * on the calendar is pressed.
     * @param view the current view to display to
     * @param date the date pressed on the calendar, so this should display the subscriptions
     *             that have a payment due on that date
     */
    private void updateSubList(View view, Date date) {
        LinearLayout linearLayout = view.findViewById(R.id.calendar_linear_layout);
        removeOnlySubViews(linearLayout);
        List<Subscription> subsDueList = model.getSubsDueOnDate(date);
        for (final Subscription sub : subsDueList) {
            final SubscriptionView subView = new SubscriptionView(getContext(), sub);
            subView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Bundle savedState = null;
                    if (getActivity() != null)
                        savedState = ((MainActivity)getActivity()).gatherSavedState();
                    Intent intent = CreateSubscriptionActivity.buildGeneralCreateIntent(getContext(),
                            CreateSubscriptionActivity.PAGE_TYPE.VIEW, sub, sub.getId(), savedState);
                    startActivity(intent);
                }
            });
            linearLayout.addView(subView);
        }
    }

    /**
     * Given a linear layout, remove from it only child views that are SubscriptionView
     * objects. This will leave any views that aren't subscription views still in the
     * layout.
     * @param linearLayout the linear layout to modify in place
     */
    private void removeOnlySubViews(LinearLayout linearLayout) {
        List<SubscriptionView> viewsToRemove = new ArrayList<>();

        // Compile all subscription views in this layout into a list
        for (int viewIdx = 0; viewIdx < linearLayout.getChildCount(); viewIdx++) {
            View childView = linearLayout.getChildAt(viewIdx);
            if (childView instanceof SubscriptionView) {
                viewsToRemove.add((SubscriptionView)childView);
            }
        }

        // Remove the previously compiled subscription views from the layout one by one
        for (SubscriptionView subView : viewsToRemove) {
            linearLayout.removeView(subView);
        }
    }

    /**
     * The full procedure the calendar tab does each time a date is pressed. First, the
     * list of subscriptions due on the date pressed will be updated, then the text above
     * that list will be changed to reflect the date selected.
     * @param date the date selected on the calendar
     * @param root the root view of this tab
     */
    private void updateCalendarTabOnDayPress(Date date, View root) {
        updateSubList(root, date);
        final ScrollView scrollView = root.findViewById(R.id.calendar_scroll_view);
        scrollView.post(new Runnable() {
            @Override
            public void run() {
                scrollView.scrollTo(0, 0);
            }
        });
        TextView dateText = root.findViewById(R.id.calendar_date_text_view);
        String displayStr = getString(R.string.calendar_list_text_prefix) + " " +
                new SimpleDateFormat(getString(R.string.date_format), Locale.US).format(date)
                + ":";
        dateText.setText(displayStr);
    }

    /**
     * Create a calendar on the system calendar that this app can add events to.
     * @param context the current application context
     * @param name the name of the calendar
     */
    private void createSyncCalendar(Context context, String name) {
        // Create the target URI
        Uri target = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        target = target.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, getString(R.string.app_name))
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDAR_ACCOUNT_TYPE).build();

        // Add all the necessary values for it
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, getString(R.string.app_name));
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDAR_ACCOUNT_TYPE);
        values.put(CalendarContract.Calendars.NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, name);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, context.getResources().getColor(R.color.colorPrimary));
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_ROOT);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, getString(R.string.app_name));
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        values.put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().toString());
        values.put(CalendarContract.Calendars.CAN_PARTIALLY_UPDATE, 1);

        // Create the calendar
        context.getContentResolver().insert(target, values);
    }

    /**
     * Delete the system calendar that was created by this app, if it exists. If it doesn't
     * exist, this won't do anything.
     * @param context the current application context
     */
    private void deleteSyncCalendar(Context context) {
        Uri uri = Uri.parse(CalendarContract.Calendars.CONTENT_URI.toString());
        uri = uri.buildUpon().appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, getString(R.string.app_name))
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CALENDAR_ACCOUNT_TYPE).build();
        context.getContentResolver().delete(uri, null, null);
    }

    /**
     * Add a set of events to the system calendar. This will loop through the next payment dates
     * for the given subscription and add an event to the calendar for each one.
     * @param context the current application context
     * @param calID the ID of the calendar the events will be added to
     * @param subscription the subscription to get next payment dates from
     * @throws SecurityException thrown if the app doesn't have permission to create events
     */
    private void createSyncCalendarEvents(Context context, int calID, Subscription subscription) throws SecurityException {
        for (Date paymentDate : subscription.getNextPaymentList()) {
            // Fill the content values with all the info needed for this event
            ContentValues cv = new ContentValues();
            cv.put(CalendarContract.Events.TITLE, subscription.getName() + " " + getString(R.string.calendar_sync_name_suffix));
            cv.put(CalendarContract.Events.DTSTART, paymentDate.getTime());
            cv.put(CalendarContract.Events.DTEND, paymentDate.getTime());
            cv.put(CalendarContract.Events.CALENDAR_ID, calID);
            cv.put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().toString());
            cv.put(CalendarContract.Events.ALL_DAY, true);

            // Create the event
            context.getContentResolver().insert(CalendarContract.Events.CONTENT_URI, cv);
        }
    }

    /**
     * Puts in a request for the read and write calendar permissions from the system, which the
     * user must accept or deny. If indicated, this can also give additional information about
     * why the permissions are needed before giving the option to choose.
     * @param parentActivity the parent activity of this fragment
     */
    private void requestCalendarPermissions(final Activity parentActivity) {
        // Request the read and write permissions
        if (ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, Manifest.permission.READ_CALENDAR) ||
                ActivityCompat.shouldShowRequestPermissionRationale(parentActivity, Manifest.permission.WRITE_CALENDAR)) {
            View view = getView();
            assert view != null;
            Snackbar.make(view, R.string.calendar_sync_permissions, Snackbar.LENGTH_INDEFINITE)
                    .setAction(R.string.ok, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            ActivityCompat.requestPermissions(parentActivity,
                                    new String[]{Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR}, PERMISSION_CALENDAR_REQUEST_CODE);
                        }
            }).show();
        } else {
            ActivityCompat.requestPermissions(parentActivity, new String[]{Manifest.permission.READ_CALENDAR,
                    Manifest.permission.WRITE_CALENDAR}, PERMISSION_CALENDAR_REQUEST_CODE);
        }
    }
}