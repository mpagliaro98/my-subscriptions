package com.mpagliaro98.mysubscriptions.model;

import android.content.res.Resources;
import com.mpagliaro98.mysubscriptions.R;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

/**
 * Subscription value object to store data on an individual subscription.
 * Every field needs a getter and setter so this object can be written to
 * and from files in json form.
 */
public class Subscription implements Serializable {

    // TODO: add categories and notification settings
    private int id;
    private String name;
    private double cost;
    private Date startDate;
    private String note;
    private String rechargeFrequency;
    private Date nextPaymentDate;
    private Category category;

    /**
     * Create and initialize all the values of this subscription.
     * @param id the unique id of this subscription
     * @param name the name of the subscription
     * @param cost how much it costs
     * @param startDate when the subscription first started
     * @param note any miscellaneous notes
     * @param rechargeFrequency the frequency at which this subscription is paid for
     * @param resources the current application resources
     * @param category the category this subscription falls into
     */
    public Subscription(int id, String name, double cost, Date startDate, String note,
                        String rechargeFrequency, Category category, Resources resources) {
        this(id, name, cost, startDate, note, rechargeFrequency, category);
        generateNextPaymentDate(resources);
    }
    public Subscription(int id, String name, double cost, Date startDate, String note,
                        String rechargeFrequency, Category category) {
        this.id = id;
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.note = note;
        this.rechargeFrequency = rechargeFrequency;
        this.nextPaymentDate = startDate;
        this.category = category;
    }

    /**
     * Calculate when the next soonest payment date will be from today.
     * @param resources the current application resources
     */
    public void generateNextPaymentDate(Resources resources) {
        Calendar c = Calendar.getInstance();
        Date today = c.getTime();
        c.setTime(startDate);
        while (!nextPaymentDate.after(today)) {
            if (rechargeFrequency.equals(resources.getString(R.string.array_freq_monthly))) {
                c.add(Calendar.MONTH, 1);
            } else if (rechargeFrequency.equals(resources.getString(R.string.array_freq_bimonthly))) {
                c.add(Calendar.MONTH, 2);
            } else if (rechargeFrequency.equals(resources.getString(R.string.array_freq_trimonthly))) {
                c.add(Calendar.MONTH, 3);
            } else if (rechargeFrequency.equals(resources.getString(R.string.array_freq_twiceyear))) {
                c.add(Calendar.MONTH, 6);
            } else if (rechargeFrequency.equals(resources.getString(R.string.array_freq_yearly))) {
                c.add(Calendar.YEAR, 1);
            }
            nextPaymentDate = c.getTime();
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
     * Get the frequency at which this subscription is paid for.
     * @return the frequency as a string
     */
    public String getRechargeFrequency() {
        return rechargeFrequency;
    }

    /**
     * Set the frequency of this subscription.
     * @param rechargeFrequency the frequency as a string
     */
    public void setRechargeFrequency(String rechargeFrequency) {
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
}
