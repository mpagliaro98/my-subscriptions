package com.mpagliaro98.mysubscriptions.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Subscription value object to store data on an individual subscription.
 * Every field needs a getter and setter so this object can be written to
 * and from files in json form.
 */
public class Subscription implements Serializable {

    public static final String dateFormat = "MM/dd/yyyy";

    private int id;
    private String name;
    private double cost;
    private Date startDate;
    private String note;
    private int rechargeFrequency;
    private Date nextPaymentDate;
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
        this.category = category;
        this.notifDays = notifDays;
        regenerateSubInfo();
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
     * Calculate when the next soonest payment date will be from today.
     */
    private void generateNextPaymentDate() {
        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        c.setTime(startDate);
        while (!nextPaymentDate.after(today)) {
            c.add(Calendar.MONTH, rechargeFrequency);
            nextPaymentDate = c.getTime();
        }
    }

    /**
     * Generate the next date a notification should occur for this subscription. This should
     * be run after the next payment date is generated. If notifications are set to off for
     * this subscription, the next notification date will be set to null.
     */
    private void generateNextNotifDate() {
        Calendar c = Calendar.getInstance();
        c.setTime(nextPaymentDate);
        if (notifDays == -1) {
            nextNotifDate = null;
        } else {
            c.add(Calendar.DATE, notifDays * -1);
            nextNotifDate = c.getTime();
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
     * Get a string representation of the cost, formatted as $X.XX.
     * @return a string representation of the cost
     */
    public String getCostString() {
        return String.format(Locale.US, "$%.2f", cost);
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
     * Get the start date in a string representation: MM/DD/YYYY.
     * @return the start date as a string
     */
    public String getStartDateString() {
        return new SimpleDateFormat(dateFormat, Locale.US).format(startDate);
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
     * Get the next payment date in a string representation: MM/DD/YYYY.
     * @return the next payment date as a string
     */
    public String getNextPaymentDateString() {
        return new SimpleDateFormat(dateFormat, Locale.US).format(nextPaymentDate);
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
