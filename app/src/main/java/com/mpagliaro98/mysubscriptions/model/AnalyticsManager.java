package com.mpagliaro98.mysubscriptions.model;

import java.util.Date;

/**
 * The class that computes analytics given a model full of subscriptions.
 */
public class AnalyticsManager {

    // The model that contains all subscription data
    private SharedViewModel model;

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
    }

    /**
     * Calculates the analytic for total amount due in the current month.
     * @return the total dollar amount due this month as a double
     */
    public double calculateTotalThisMonth() {
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
     * @return the total dollar amount due yearly as a double
     */
    public double calculateTotalDueYearly() {
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
     * @return the total dollar amount of the most expensive yearly subscription as a double
     */
    public double calculateMostExpensiveCost() {
        double costMostExpensive = 0;
        for (Subscription sub : model.getFullSubscriptionList()) {
            int multiplier = 12 / sub.getRechargeFrequency();
            double totalDueYearly = sub.getCost() * multiplier;
            if (totalDueYearly > costMostExpensive) {
                costMostExpensive = totalDueYearly;
            }
        }
        return costMostExpensive;
    }

    /**
     * Calculates the most expensive subscription per year, and returns the name of that
     * subscription.
     * @return the name of the most expensive yearly subscription as a string
     */
    public String getMostExpensiveName() {
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
        return nameMostExpensive;
    }
}
