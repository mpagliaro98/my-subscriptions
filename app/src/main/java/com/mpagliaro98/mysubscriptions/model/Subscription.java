package com.mpagliaro98.mysubscriptions.model;

import android.content.Context;
import android.content.res.Resources;
import com.mpagliaro98.mysubscriptions.R;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Subscription value object to store data on an individual subscription.
 * Every field needs a getter and setter so this object can be written to
 * and from files in json form.
 */
public class Subscription implements Serializable {

    // The number of years ahead next payment dates will be generated for
    private static final int MAX_YEARS_AHEAD = 5;

    private int id;
    private String name;
    private double cost;
    private Date startDate;
    private String note;
    private int rechargeFrequency;
    private Date nextPaymentDate;
    private ArrayList<Date> nextPaymentList;
    private Category category;
    private int notifDays;
    private Date nextNotifDate;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create and initialize all the values of this subscription.
     * @param id the unique id of this subscription
     * @param name the name of the subscription
     * @param cost how much it costs
     * @param startDate when the subscription first started
     * @param note any miscellaneous notes
     * @param rechargeFrequency the frequency at which this subscription is paid for
     * @param category the category this subscription falls into
     * @param notifDays the number of days before the next payment date a notification will happen
     */
    public Subscription(int id, String name, double cost, Date startDate, String note,
                        int rechargeFrequency, Category category, int notifDays) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.note = note;
        this.rechargeFrequency = rechargeFrequency;
        this.nextPaymentDate = startDate;
        this.nextPaymentList = new ArrayList<>();
        this.category = category;
        this.notifDays = notifDays;
        regenerateSubInfo();
    }
    /**
     * Create and initialize all the values of this subscription.
     * @param id the unique id of this subscription
     * @param name the name of the subscription
     * @param cost how much it costs
     * @param startDate when the subscription first started
     * @param note any miscellaneous notes
     * @param rechargeFrequency the frequency at which this subscription is paid for
     * @param category the category this subscription falls into
     * @param notifDays the number of days before the next payment date a notification will happen
     * @param zeroTimeCalendar a calendar of today's date with the time set to 0:00:00
     */
    public Subscription(int id, String name, double cost, Date startDate, String note,
                        int rechargeFrequency, Category category, int notifDays,
                        ZeroTimeCalendar zeroTimeCalendar) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.note = note;
        this.rechargeFrequency = rechargeFrequency;
        this.nextPaymentDate = startDate;
        this.nextPaymentList = new ArrayList<>();
        this.category = category;
        this.notifDays = notifDays;
        regenerateSubInfo(zeroTimeCalendar);
    }

    /**
     * Regenerate each subscription field that isn't directly specified on creation, or needs
     * to be updated after a certain amount of time passes.
     */
    public void regenerateSubInfo() {
        generateNextPaymentDate();
        generateNextNotifDate();
    }
    /**
     * Regenerate each subscription field that isn't directly specified on creation, or needs
     * to be updated after a certain amount of time passes.
     * @param zeroTimeCalendar a calendar of today's date with the time set to 0:00:00
     */
    public void regenerateSubInfo(ZeroTimeCalendar zeroTimeCalendar) {
        generateNextPaymentDate(zeroTimeCalendar);
        generateNextNotifDate(zeroTimeCalendar);
    }

    /**
     * Calculate when the next soonest payment date will be from today, as well as a list
     * of payment dates after that for the next MAX_YEARS_AHEAD years.
     */
    private void generateNextPaymentDate() {
        generateNextPaymentDate(new ZeroTimeCalendar());
    }
    /**
     * Calculate when the next soonest payment date will be from today, as well as a list
     * of payment dates after that for the next MAX_YEARS_AHEAD years.
     * @param zeroTimeCalendar a calendar of today's date with the time set to 0:00:00
     */
    private void generateNextPaymentDate(ZeroTimeCalendar zeroTimeCalendar) {
        nextPaymentList = new ArrayList<>();
        Date today = zeroTimeCalendar.getCurrentDate();
        Date nextPaymentDate = startDate;
        zeroTimeCalendar.setTimeToDate(startDate);

        // Loop forward until we find the first next payment date
        while (!nextPaymentDate.after(today) && !nextPaymentDate.equals(today)) {
            zeroTimeCalendar.addMonths(rechargeFrequency);
            nextPaymentDate = zeroTimeCalendar.getCurrentDate();
        }

        // Start the list with this payment date
        nextPaymentList.add(nextPaymentDate);
        zeroTimeCalendar.addYears(MAX_YEARS_AHEAD);
        Date futureLimit = zeroTimeCalendar.getCurrentDate();
        zeroTimeCalendar.setTimeToDate(nextPaymentDate);

        // Find each payment date between now and the future limit
        while (nextPaymentDate.before(futureLimit)) {
            zeroTimeCalendar.addMonths(rechargeFrequency);
            nextPaymentDate = zeroTimeCalendar.getCurrentDate();
            nextPaymentList.add(nextPaymentDate);
        }
        this.nextPaymentDate = nextPaymentList.get(0);
    }

    /**
     * Generate the next date a notification should occur for this subscription. This should
     * be run after the next payment date is generated. If notifications are set to off for
     * this subscription, the next notification date will be set to null.
     */
    private void generateNextNotifDate() {
        generateNextNotifDate(new ZeroTimeCalendar());
    }
    /**
     * Generate the next date a notification should occur for this subscription. This should
     * be run after the next payment date is generated. If notifications are set to off for
     * this subscription, the next notification date will be set to null.
     * @param zeroTimeCalendar a calendar of today's date with the time set to 0:00:00
     */
    private void generateNextNotifDate(ZeroTimeCalendar zeroTimeCalendar) {
        zeroTimeCalendar.setTimeToDate(nextPaymentDate);
        if (notifDays == -1) {
            nextNotifDate = null;
        } else {
            zeroTimeCalendar.addDays(notifDays * -1);
            nextNotifDate = zeroTimeCalendar.getCurrentDate();
        }
    }

    /**
     * Get the unique ID of this subscription.
     * @return the ID as an int
     */
    public int getId() {
        return id;
    }

    /**
     * Set the unique ID of this subscription.
     * @param id the ID as an int
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get the name of the subscription.
     * @return the name as a string
     */
    public String getName() {
        return name;
    }

    /**
     * Set the name of the subscription.
     * @param name the name as a string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the cost of the subscription.
     * @return the cost as a double
     */
    public double getCost() {
        return cost;
    }

    /**
     * Get a string representation of the cost based on how cost_format is set. This will use the
     * currency symbol used in the settings, but if that fails, will default to $.
     * @param context the current application context, containing the resources that has
     *                the cost_format string
     * @return a string representation of the cost
     */
    public String getCostString(Context context) {
        String currencySymbol;
        try {
            SettingsManager settingsManager = new SettingsManager(context);
            currencySymbol = settingsManager.getCurrencySymbol();
        } catch (IOException e) {
            currencySymbol = context.getResources().getString(R.string.currency_default);
        }
        String costString = String.format(Locale.US, context.getResources().getString(R.string.cost_format), cost);
        return currencySymbol + costString;
    }

    /**
     * Set the cost of the subscription.
     * @param cost the cost as a double
     */
    public void setCost(double cost) {
        this.cost = cost;
    }

    /**
     * Get the start date of the subscription.
     * @return the start date as a Date object
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Get the start date in a string representation based on how date_format is set.
     * @param resources the resources containing the date_format string
     * @return the start date as a string
     */
    public String getStartDateString(Resources resources) {
        return new SimpleDateFormat(resources.getString(R.string.date_format), Locale.US).format(startDate);
    }

    /**
     * Set the start date of the subscription.
     * @param startDate the start date as a Date object
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * Get the note attached to the subscription.
     * @return the note as a string
     */
    public String getNote() {
        return note;
    }

    /**
     * Set a note to this subscription.
     * @param note the note as a string
     */
    public void setNote(String note) {
        this.note = note;
    }

    /**
     * Get the frequency at which this subscription is paid for in months.
     * @return the frequency as an int
     */
    public int getRechargeFrequency() {
        return rechargeFrequency;
    }

    /**
     * Set the frequency of this subscription.
     * @param rechargeFrequency the frequency as an int
     */
    public void setRechargeFrequency(int rechargeFrequency) {
        this.rechargeFrequency = rechargeFrequency;
    }

    /**
     * Get the next immediate date this Subscription will be charged.
     * @return the next payment date
     */
    public Date getNextPaymentDate() {
        return nextPaymentDate;
    }

    /**
     * Get the next payment date in a string representation based on how date_format is set.
     * @param resources the resources containing the date_format string
     * @return the next payment date as a string
     */
    public String getNextPaymentDateString(Resources resources) {
        return new SimpleDateFormat(resources.getString(R.string.date_format), Locale.US).format(nextPaymentDate);
    }

    /**
     * Get a list of the future payment dates for this subscription for the next X amount
     * of years, as defined by MAX_YEARS_AHEAD in this class.
     * @return a list of next payment dates
     */
    public ArrayList<Date> getNextPaymentList() {
        return nextPaymentList;
    }

    /**
     * Get the category of this subscription.
     * @return the category object
     */
    public Category getCategory() {
        return category;
    }

    /**
     * Set the category of this subscription.
     * @param category the category object
     */
    public void setCategory(Category category) {
        this.category = category;
    }

    /**
     * Get the number of days before the next payment date to get a notification.
     * @return the number of days as an int
     */
    public int getNotifDays() {
        return notifDays;
    }

    /**
     * Set the number of days before the next payment date to get a notification.
     * @param notifDays the number of days as an int
     */
    public void setNotifDays(int notifDays) {
        this.notifDays = notifDays;
    }

    /**
     * Get the next date this subscription should be notified.
     * @return the next notification date as a Date object
     */
    public Date getNextNotifDate() {
        return nextNotifDate;
    }
}
