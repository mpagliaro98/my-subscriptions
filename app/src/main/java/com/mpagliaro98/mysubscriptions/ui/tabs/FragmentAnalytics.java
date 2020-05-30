package com.mpagliaro98.mysubscriptions.ui.tabs;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.lifecycle.ViewModelProvider;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.AnalyticsManager;
import com.mpagliaro98.mysubscriptions.model.Category;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import com.mpagliaro98.mysubscriptions.ui.interfaces.SavedStateCompatible;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
        AnalyticsManager analyticsManager = new AnalyticsManager(model);
        calculateAnalytics(root, analyticsManager);

        Spinner breakdownDropdown = root.findViewById(R.id.analytics_breakdown_dropdown);
        addBreakdownDropdownListener(breakdownDropdown, root, analyticsManager);
        updatePieChart(root, analyticsManager);
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
     * @param analyticsManager the object using the model to generate analytics
     */
    private void calculateAnalytics(View root, AnalyticsManager analyticsManager) {
        // Calculate the total due this month
        double totalDueThisMonth = analyticsManager.getTotalDueThisMonth();
        TextView textDueThisMonth = root.findViewById(R.id.analytics_data_thismonth);
        textDueThisMonth.setText(String.format(Locale.US, getString(R.string.cost_format), totalDueThisMonth));

        // Calculate the rest due this month
        double restDueThisMonth = analyticsManager.getRestDueThisMonth();
        TextView textRestDueThisMonth = root.findViewById(R.id.analytics_data_restofmonth);
        textRestDueThisMonth.setText(String.format(Locale.US, getString(R.string.cost_format), restDueThisMonth));

        // Calculate the total due next month
        double totalDueNextMonth = analyticsManager.getTotalDueNextMonth();
        TextView textDueNextMonth = root.findViewById(R.id.analytics_data_nextmonth);
        textDueNextMonth.setText(String.format(Locale.US, getString(R.string.cost_format), totalDueNextMonth));

        // Calculate the total due yearly
        double totalDueYearly = analyticsManager.getTotalDueYearly();
        TextView textDueYearly = root.findViewById(R.id.analytics_data_yearly);
        textDueYearly.setText(String.format(Locale.US, getString(R.string.cost_format), totalDueYearly));

        // Find the most expensive yearly subscription
        double costMostExpensive = analyticsManager.getCostMostExpensive();
        String nameMostExpensive = analyticsManager.getNameMostExpensive();
        TextView textMostExpensive = root.findViewById(R.id.analytics_data_mostexpensive);
        String displayStr = nameMostExpensive + ": " +
                String.format(Locale.US, getString(R.string.cost_format), costMostExpensive) +
                " " + getString(R.string.analytics_mostexpensive_per_year);
        textMostExpensive.setText(displayStr);

        // Find the most common recharge frequency
        int mostCommonFrequency = analyticsManager.getMostCommonRecharge();
        TextView textFrequency = root.findViewById(R.id.analytics_data_frequentrecharge);
        if (mostCommonFrequency == 1) {
            textFrequency.setText(R.string.analytics_recharge_onemonth);
        } else if (mostCommonFrequency == 2) {
            textFrequency.setText(R.string.analytics_recharge_twomonths);
        } else if (mostCommonFrequency == 3) {
            textFrequency.setText(R.string.analytics_recharge_threemonths);
        } else if (mostCommonFrequency == 6) {
            textFrequency.setText(R.string.analytics_recharge_sixmonths);
        } else if (mostCommonFrequency == 12) {
            textFrequency.setText(R.string.analytics_recharge_year);
        } else {
            // If there isn't one most common frequency, remove this portion from the view
            ConstraintLayout subLayout = root.findViewById(R.id.analytics_sublayout6);
            subLayout.removeAllViews();
            ((ViewGroup)subLayout.getParent()).removeView(subLayout);
            View horizontal = root.findViewById(R.id.horizontal5);
            ((ViewGroup)horizontal.getParent()).removeView(horizontal);
            // Re-wire the constraints in the absence of this section
            ConstraintLayout parentLayout = root.findViewById(R.id.analytics_constr_layout);
            ConstraintSet constraintSet = new ConstraintSet();
            constraintSet.clone(parentLayout);
            float density = getResources().getDisplayMetrics().density;
            int marginPx = Math.round((float) 16 * density);
            constraintSet.connect(R.id.analytics_sublayout5, ConstraintSet.BOTTOM,
                    R.id.analytics_constr_layout, ConstraintSet.BOTTOM, marginPx);
            System.out.println(constraintSet.toString());
            constraintSet.applyTo(parentLayout);
        }
    }

    /**
     * Adds a listener to the category breakdown dropdown list, which each time it's updated,
     * will re-calculate how much is owed in the selected time period for each category.
     * @param breakdownDropdown the dropdown list
     * @param root the root view of this tab
     * @param analyticsManager the analytics manager
     */
    private void addBreakdownDropdownListener(Spinner breakdownDropdown, final View root,
                                              final AnalyticsManager analyticsManager) {
        breakdownDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOption = (String)parent.getSelectedItem();
                if (sortOption.equals(getString(R.string.array_breakdown_one_month))) {
                    analyticsManager.createMonthlyBreakdown(1);
                } else if (sortOption.equals(getString(R.string.array_breakdown_two_months))) {
                    analyticsManager.createMonthlyBreakdown(2);
                } else if (sortOption.equals(getString(R.string.array_breakdown_three_months))) {
                    analyticsManager.createMonthlyBreakdown(3);
                } else if (sortOption.equals(getString(R.string.array_breakdown_six_months))) {
                    analyticsManager.createMonthlyBreakdown(6);
                } else if (sortOption.equals(getString(R.string.array_breakdown_one_year))) {
                    analyticsManager.createMonthlyBreakdown(12);
                }
                updatePieChart(root, analyticsManager);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Refresh the pie chart and fill it with the breakdown data in the given
     * analytics manager. This will automatically update the chart in the view.
     * @param root the root view of this tab
     * @param analyticsManager the analytics manager
     */
    private void updatePieChart(View root, AnalyticsManager analyticsManager) {
        PieChart pieChart = root.findViewById(R.id.analytics_pie_chart);
        pieChart.clear();
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHoleColor(getResources().getColor(R.color.colorLightGreyBG));
        pieChart.setEntryLabelColor(R.color.solid_black);
        List<PieEntry> chartValues = new ArrayList<>();
        List<Integer> chartColors = new ArrayList<>();

        // Add each category, dollar value, and color to lists to be used by the chart
        for (Map.Entry<Category, Double> entry : analyticsManager.getBreakdownList()) {
            chartValues.add(new PieEntry(entry.getValue().floatValue(), entry.getKey().getName()));
            chartColors.add(getResources().getColor(entry.getKey().getColor()));
        }

        // Set all the chart data and legend properties
        PieDataSet pieDataSet = new PieDataSet(chartValues, getString(R.string.analytics_legend_title));
        pieDataSet.setColors(chartColors);
        Legend legend = pieChart.getLegend();
        legend.setWordWrapEnabled(true);
        legend.setOrientation(Legend.LegendOrientation.VERTICAL);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.LEFT);
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.CENTER);

        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
    }
}