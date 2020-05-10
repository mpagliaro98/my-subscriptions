package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.CreateSubscriptionActivity;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionCalendar;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionView;
import com.mpagliaro98.mysubscriptions.ui.interfaces.CalendarEventHandler;
import com.mpagliaro98.mysubscriptions.ui.interfaces.SavedStateCompatible;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

/**
 * A fragment containing the view for the calendar tab.
 */
public class FragmentCalendar extends Fragment implements SavedStateCompatible {

    // The model shared by the three main tabs
    private SharedViewModel model;
    // Any saved state from previously in the application to apply when loading the view
    private Bundle savedState;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create this fragment object and set its saved state bundle to be used later.
     * @param savedState bundle of saved state
     */
    public FragmentCalendar(Bundle savedState) {
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
            nextPaymentDates.addAll(sub.getNextPaymentList());
        }

        // Update the calendar using the set of next payment dates as the events
        SubscriptionCalendar subCalendar = root.findViewById(R.id.subscriptionCalendar);
        subCalendar.setEvents(nextPaymentDates);
        subCalendar.updateCalendar();

        // Set a listener for the calendar
        subCalendar.setCalendarEventHandler(new CalendarEventHandler() {
            @Override
            public void onDayPress(Date date) {
                updateSubList(root, date);
            }
        });
        return root;
    }

    /**
     * Populate a given bundle with values pertaining to how this fragment is set.
     * @param bundle the bundle to place the saved items in
     */
    @Override
    public void fillBundleWithSavedState(Bundle bundle) {

    }

    /**
     * Given a bundle of saved state, extract the values that were saved to it previously
     * and re-apply them to this view.
     * @param savedState bundle of saved state, must not be null
     * @param root the root view of this tab
     */
    @Override
    public void applySavedState(@NonNull final Bundle savedState, View root) {

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
        LinearLayout linearLayout = view.findViewById(R.id.calendarLinearLayout);
        linearLayout.removeAllViewsInLayout();
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
}