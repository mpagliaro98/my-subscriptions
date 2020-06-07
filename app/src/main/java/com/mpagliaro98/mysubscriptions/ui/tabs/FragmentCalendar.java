package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.model.ZeroTimeCalendar;
import com.mpagliaro98.mysubscriptions.ui.CreateSubscriptionActivity;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionCalendar;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionView;
import com.mpagliaro98.mysubscriptions.ui.interfaces.CalendarEventHandler;
import com.mpagliaro98.mysubscriptions.ui.interfaces.SavedStateCompatible;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

/**
 * A fragment containing the view for the calendar tab.
 */
public class FragmentCalendar extends Fragment implements SavedStateCompatible {

    // The model shared by the three main tabs
    private SharedViewModel model;
    // Any saved state from previously in the application to apply when loading the view
    private Bundle savedState;

    // Keys for the saved state of the calendar fragment when returning
    public static final String SAVED_STATE_MONTH_MESSAGE = "com.mpagliaro98.mysubscriptions.C_SAVED_MONTH";
    public static final String SAVED_STATE_SELECTED_DATE_MESSAGE = "com.mpagliaro98.mysubscriptions.C_SAVED_SELECTED_DATE";
    public static final String SAVED_STATE_SCROLL_MESSAGE = "com.mpagliaro98.mysubscriptions.C_SAVED_SCROLL";

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
}