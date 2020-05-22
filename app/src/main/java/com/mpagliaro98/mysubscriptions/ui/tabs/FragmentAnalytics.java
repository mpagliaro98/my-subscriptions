package com.mpagliaro98.mysubscriptions.ui.tabs;

import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.model.ZeroTimeCalendar;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import com.mpagliaro98.mysubscriptions.ui.interfaces.SavedStateCompatible;
import java.util.Date;
import java.util.Locale;

/**
 * A fragment containing the view for the analytics tab.
 */
public class FragmentAnalytics extends Fragment implements SavedStateCompatible {

    // The model shared by the three main tabs
    private SharedViewModel model;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Initializes the model for the analytics tab.
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
        View root = inflater.inflate(R.layout.fragment_analytics_tab, container, false);
        calculateAnalytics(root, model);
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
     * Calculate the set of analytics that get displayed in the main layout on the
     * analytics tab. This calculates each analytic, then adds it to its respective
     * view.
     * @param root the root view of this tab
     * @param model the model containing all subscription data
     */
    private void calculateAnalytics(View root, SharedViewModel model) {
        // Calculate the total due this month
        double totalDueThisMonth = calculateTotalThisMonth(model);
        TextView textDueThisMonth = root.findViewById(R.id.analytics_data_thismonth);
        textDueThisMonth.setText(String.format(Locale.US, getString(R.string.cost_format), totalDueThisMonth));

        // Calculate the total due yearly
        double totalDueYearly = calculateTotalDueYearly(model);
        TextView textDueYearly = root.findViewById(R.id.analytics_data_yearly);
        textDueYearly.setText(String.format(Locale.US, getString(R.string.cost_format), totalDueYearly));

        // Find the most expensive yearly subscription
        double costMostExpensive = calculateMostExpensiveCost(model);
        String nameMostExpensive = getMostExpensiveName(model);
        TextView textMostExpensive = root.findViewById(R.id.analytics_data_mostexpensive);
        String displayStr = nameMostExpensive + ": " +
                String.format(Locale.US, getString(R.string.cost_format), costMostExpensive) +
                " " + getString(R.string.analytics_mostexpensive_per_year);
        textMostExpensive.setText(displayStr);
    }

    /**
     * Calculates the analytic for total amount due in the current month.
     * @param model the model containing all subscription data
     * @return the total dollar amount due this month as a double
     */
    private double calculateTotalThisMonth(SharedViewModel model) {
        double totalDueThisMonth = 0;
        ZeroTimeCalendar calendarToday = new ZeroTimeCalendar();
        for (Subscription sub : model.getFullSubscriptionList()) {
            Date nextPaymentDate = sub.getNextPaymentDate();
            ZeroTimeCalendar calendarSub = new ZeroTimeCalendar();
            calendarSub.setTimeToDate(nextPaymentDate);
            if (calendarToday.getMonth() == calendarSub.getMonth() &&
                    calendarToday.getYear() == calendarSub.getYear()) {
                totalDueThisMonth += sub.getCost();
            }
        }
        return totalDueThisMonth;
    }

    /**
     * Calculates the analytic for total amount due per year, which also finds the yearly
     * total for subscriptions that aren't explicitly yearly.
     * @param model the model containing all subscription data
     * @return the total dollar amount due yearly as a double
     */
    private double calculateTotalDueYearly(SharedViewModel model) {
        double totalDueYearly = 0;
        for (Subscription sub : model.getFullSubscriptionList()) {
            int multiplier = 12 / sub.getRechargeFrequency();
            totalDueYearly += sub.getCost() * multiplier;
        }
        return totalDueYearly;
    }

    /**
     * Calculates the analytic for the most expensive subscription per year, and returns
     * the highest amount per year a single subscription is worth.
     * @param model the model containing all subscription data
     * @return the total dollar amount of the most expensive yearly subscription as a double
     */
    private double calculateMostExpensiveCost(SharedViewModel model) {
        return 0;
    }

    /**
     * Calculates the most expensive subscription per year, and returns the name of that
     * subscription.
     * @param model the model containing all subscription data
     * @return the name of the most expensive yearly subscription as a string
     */
    private String getMostExpensiveName(SharedViewModel model) {
        return "";
    }
}