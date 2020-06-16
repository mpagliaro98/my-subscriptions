package com.mpagliaro98.mysubscriptions.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The class that computes analytics given a model full of subscriptions.
 */
public class AnalyticsManager {

    // The model that contains all subscription data
    private SharedViewModel model;

    // A base calendar that each method will clone from to get the time
    private ZeroTimeCalendar baseZTC;

    // The analytics values we want to keep track of
    private double totalDueThisMonth;
    private double restDueThisMonth;
    private double totalDueNextMonth;
    private double totalDueYearly;
    private double costMostExpensive;
    private String nameMostExpensive;
    private int mostCommonRecharge;
    private HashMap<Category, Double> breakdown;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create the analytics manager and calculate the suite of analytics using the given
     * model. This will also create a default breakdown using one month.
     * @param model the model containing all subscription data
     */
    public AnalyticsManager(SharedViewModel model) {
        this.model = model;
        this.baseZTC = new ZeroTimeCalendar();
        calculateAnalytics();
        createMonthlyBreakdown(1);
    }

    /**
     * Create the analytics manager and calculate the suite of analytics using the given
     * model. This will also create a default breakdown using one month.
     * @param model the model containing all subscription data
     * @param zeroTimeCalendar a zero time calendar set to the date that should be considered
     *                         today's date. This is primarily used for testing.
     */
    public AnalyticsManager(SharedViewModel model, ZeroTimeCalendar zeroTimeCalendar) {
        this.model = model;
        this.baseZTC = zeroTimeCalendar;
        calculateAnalytics();
        createMonthlyBreakdown(1);
    }

    /**
     * Run the suite of analytics again and update the internal values.
     */
    public void regenerateAnalytics() {
        calculateAnalytics();
    }

    /**
     * Create the category breakdown HashMap, and fill it with the values it needs pertaining
     * to the length of the period passed in. This will calculate how much is owed for each
     * category over a period of several months, starting from the current month.
     * @param months the amount of months (starting with this month) to look at into the future
     */
    public void createMonthlyBreakdown(int months) {
        this.breakdown = new HashMap<>();
        ZeroTimeCalendar calendarToday;
        for (Subscription sub : model.getFullSubscriptionList()) {
            calendarToday = baseZTC.copyCalendar();
            Date subStartDate = sub.getStartDate();
            ZeroTimeCalendar calendarSub = baseZTC.copyCalendar();
            calendarSub.setTimeToDate(subStartDate);
            calendarToday.setTime(calendarToday.getYear(), calendarToday.getMonth(), 1);
            calendarSub.setTime(calendarSub.getYear(), calendarSub.getMonth(), 1);

            // Calculate the first potential payment date within the given period
            while (calendarSub.getCurrentDate().before(calendarToday.getCurrentDate())) {
                calendarSub.addMonths(sub.getRechargeFrequency());
            }

            // Set this date to the end date of the period
            calendarToday.addMonths(months);

            // Count how many times the subscription gets charged in this period
            double subPeriodCost = 0.0;
            while (calendarSub.getCurrentDate().before(calendarToday.getCurrentDate())) {
                calendarSub.addMonths(sub.getRechargeFrequency());
                subPeriodCost += sub.getCost();
            }

            // Put that calculated cost into the map
            if (subPeriodCost > 0) {
                if (breakdown.containsKey(sub.getCategory())) {
                    breakdown.put(sub.getCategory(), breakdown.get(sub.getCategory()) + subPeriodCost);
                } else {
                    breakdown.put(sub.getCategory(), subPeriodCost);
                }
            }
        }
    }

    /**
     * Get the total dollar amount due in the current month.
     * @return the total due this month as a double
     */
    public double getTotalDueThisMonth() {
        return totalDueThisMonth;
    }

    /**
     * Get the total dollar amount due for the rest of the current month
     * @return the total due in the rest of the month as a double
     */
    public double getRestDueThisMonth() {
        return restDueThisMonth;
    }

    /**
     * Get the total dollar amount due in the next month.
     * @return the total due next month as a double
     */
    public double getTotalDueNextMonth() {
        return totalDueNextMonth;
    }

    /**
     * Get the total dollar amount of all subscriptions due during the upcoming year.
     * @return the total yearly due as a double
     */
    public double getTotalDueYearly() {
        return totalDueYearly;
    }

    /**
     * Get the cost of the most expensive yearly subscription.
     * @return the most expensive yearly cost as a double
     */
    public double getCostMostExpensive() {
        return costMostExpensive;
    }

    /**
     * Get the name of the most expensive yearly subscription.
     * @return the name of the most expensive yearly subscription as a string
     */
    public String getNameMostExpensive() {
        return nameMostExpensive;
    }

    /**
     * Get the recharge frequency that is used most commonly, which will be the number
     * of months between charges, or 0 if there are no subscriptions or no one frequency
     * is the most common
     * @return the most common number of months between charges as an integer
     */
    public int getMostCommonRecharge() {
        return mostCommonRecharge;
    }

    /**
     * After a category breakdown has been generated, return a list of every value in the
     * breakdown as key-value pairs, with the key being the Category object and the value being
     * the total dollar amount of that category as a double. The list will be sorted with the
     * highest amount being first.
     * @return a sorted list of key-value pairs of categories and doubles
     */
    public List<Map.Entry<Category, Double>> getBreakdownList() {
        List<Map.Entry<Category, Double>> breakdownList = new ArrayList<>(breakdown.entrySet());
        Collections.sort(breakdownList, new Comparator<Map.Entry<Category, Double>>() {
            @Override
            public int compare(Map.Entry<Category, Double> o1, Map.Entry<Category, Double> o2) {
                if (o1.getValue().equals(o2.getValue()))
                    return o1.getKey().getName().compareToIgnoreCase(o2.getKey().getName());
                else
                    return o1.getValue() > o2.getValue() ? -1 : 1;
            }
        });
        return breakdownList;
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Run the whole suite of analytics, where each method called will save a computed value
     * to a field of this class.
     */
    private void calculateAnalytics() {
        calculateTotalThisMonth();
        calculateRestDueThisMonth();
        calculateTotalDueNextMonth();
        calculateTotalDueYearly();
        calculateMostExpensive();
        calculateMostCommonRecharge();
    }

    /**
     * Calculates the analytic for total amount due in the current month.
     */
    private void calculateTotalThisMonth() {
        double totalDueThisMonth = 0;
        ZeroTimeCalendar calendarToday = baseZTC.copyCalendar();
        for (Subscription sub : model.getFullSubscriptionList()) {
            Date subStartDate = sub.getStartDate();
            ZeroTimeCalendar calendarSub = baseZTC.copyCalendar();
            calendarSub.setTimeToDate(subStartDate);
            calendarToday.setTime(calendarToday.getYear(), calendarToday.getMonth(), 1);
            calendarSub.setTime(calendarSub.getYear(), calendarSub.getMonth(), 1);

            // Calculate the first potential payment date on or after this month
            while (calendarSub.getCurrentDate().before(calendarToday.getCurrentDate())) {
                calendarSub.addMonths(sub.getRechargeFrequency());
            }

            // If this sub had or will have a payment due this month, add its cost to the total
            if (calendarSub.getMonth() == calendarToday.getMonth() &&
                    calendarSub.getYear() == calendarToday.getYear()) {
                totalDueThisMonth += sub.getCost();
            }
        }
        this.totalDueThisMonth = totalDueThisMonth;
    }

    /**
     * Calculates the analytic for the total amount due during the rest of the current month.
     * If subscriptions were due earlier this month, but their date already passed, they are
     * ignored here, this just counts ones with next payment dates still during the current
     * month.
     */
    private void calculateRestDueThisMonth() {
        double restDueThisMonth = 0;
        ZeroTimeCalendar calendarToday = baseZTC.copyCalendar();
        for (Subscription sub : model.getFullSubscriptionList()) {
            Date nextPaymentDate = sub.getNextPaymentDate();
            ZeroTimeCalendar calendarSub = baseZTC.copyCalendar();
            calendarSub.setTimeToDate(nextPaymentDate);
            if (calendarToday.getMonth() == calendarSub.getMonth() &&
                    calendarToday.getYear() == calendarSub.getYear()) {
                restDueThisMonth += sub.getCost();
            }
        }
        this.restDueThisMonth = restDueThisMonth;
    }

    /**
     * Calculates the analytic for the total amount due next month.
     */
    private void calculateTotalDueNextMonth() {
        double totalDueNextMonth = 0;
        ZeroTimeCalendar calendarToday = baseZTC.copyCalendar();
        ZeroTimeCalendar calendarNextMonth = baseZTC.copyCalendar();
        calendarNextMonth.addMonths(1);
        for (Subscription sub : model.getFullSubscriptionList()) {
            Date nextPaymentDate = sub.getNextPaymentDate();
            ZeroTimeCalendar calendarSub = baseZTC.copyCalendar();
            calendarSub.setTimeToDate(nextPaymentDate);
            if ((calendarNextMonth.getMonth() == calendarSub.getMonth() &&
                  calendarNextMonth.getYear() == calendarSub.getYear()) ||
                    (calendarToday.getMonth() == calendarSub.getMonth() &&
                      calendarToday.getYear() == calendarSub.getYear() &&
                   sub.getRechargeFrequency() == 1)) {
                totalDueNextMonth += sub.getCost();
            }
        }
        this.totalDueNextMonth = totalDueNextMonth;
    }

    /**
     * Calculates the analytic for total amount due per year, which also finds the yearly
     * total for subscriptions that aren't explicitly yearly.
     */
    private void calculateTotalDueYearly() {
        double totalDueYearly = 0;
        for (Subscription sub : model.getFullSubscriptionList()) {
            int multiplier = 12 / sub.getRechargeFrequency();
            totalDueYearly += sub.getCost() * multiplier;
        }
        this.totalDueYearly = totalDueYearly;
    }

    /**
     * Calculates the most expensive subscription per year, and saves the name of that
     * subscription along with the yearly cost of it.
     */
    private void calculateMostExpensive() {
        double costMostExpensive = 0;
        String nameMostExpensive = "";
        for (Subscription sub : model.getFullSubscriptionList()) {
            int multiplier = 12 / sub.getRechargeFrequency();
            double totalDueYearly = sub.getCost() * multiplier;
            if (totalDueYearly > costMostExpensive) {
                costMostExpensive = totalDueYearly;
                nameMostExpensive = sub.getName();
            }
        }
        this.costMostExpensive = costMostExpensive;
        this.nameMostExpensive = nameMostExpensive;
    }

    /**
     * Calculates the most common recharge frequency amongst all the subscriptions, and
     * saves the number of months that is most commonly found. It will be 0 if there are
     * no subscriptions or no one rate is the maximum.
     */
    private void calculateMostCommonRecharge() {
        // Record each frequency that exists and how many times it appears
        HashMap<Integer, Integer> frequencyMap = new HashMap<>();
        for (Subscription sub : model.getFullSubscriptionList()) {
            if (frequencyMap.containsKey(sub.getRechargeFrequency())) {
                frequencyMap.put(sub.getRechargeFrequency(), frequencyMap.get(sub.getRechargeFrequency()) + 1);
            } else {
                frequencyMap.put(sub.getRechargeFrequency(), 1);
            }
        }

        // Initialize values needed when analyzing the data
        boolean tieExists = false;
        int highestFrequency = 0;
        int highestFrequencyNumber = 0;

        // Check each number of times a frequency appears and find the maximum
        for (Map.Entry<Integer, Integer> entry : frequencyMap.entrySet()) {
            if (entry.getValue() > highestFrequencyNumber) {
                highestFrequencyNumber = entry.getValue();
                highestFrequency = entry.getKey();
                tieExists = false;
            } else if (entry.getValue() == highestFrequencyNumber) {
                tieExists = true;
            }
        }

        // Save zero if no frequencies or a tie exists, and save the maximum otherwise
        if (frequencyMap.isEmpty() || tieExists) {
            this.mostCommonRecharge = 0;
        } else {
            this.mostCommonRecharge = highestFrequency;
        }
    }
}
