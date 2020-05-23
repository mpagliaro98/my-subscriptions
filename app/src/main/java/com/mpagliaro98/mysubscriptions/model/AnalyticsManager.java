package com.mpagliaro98.mysubscriptions.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * The class that computes analytics given a model full of subscriptions.
 */
public class AnalyticsManager {

    // The model that contains all subscription data
    private SharedViewModel model;

    // The analytics values we want to keep track of
    private double totalDueThisMonth;
    private double totalDueYearly;
    private double costMostExpensive;
    private String nameMostExpensive;
    private int mostCommonRecharge;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create the analytics manager, and save in it the model it will use to
     * calculate analytics later on.
     * @param model the model containing all subscription data
     */
    public AnalyticsManager(SharedViewModel model) {
        this.model = model;
        calculateAnalytics();
    }

    /**
     * Run the suite of analytics again and update the internal values.
     */
    public void regenerateAnalytics() {
        calculateAnalytics();
    }

    /**
     * Get the total dollar amount due in the current month.
     * @return the total due this month as a double
     */
    public double getTotalDueThisMonth() {
        return totalDueThisMonth;
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

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Run the whole suite of analytics, where each method called will save a computed value
     * to a field of this class.
     */
    private void calculateAnalytics() {
        calculateTotalThisMonth();
        calculateTotalDueYearly();
        calculateMostExpensive();
        calculateMostCommonRecharge();
    }

    /**
     * Calculates the analytic for total amount due in the current month.
     */
    private void calculateTotalThisMonth() {
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
        this.totalDueThisMonth = totalDueThisMonth;
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
