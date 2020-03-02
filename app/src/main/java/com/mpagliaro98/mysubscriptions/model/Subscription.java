package com.mpagliaro98.mysubscriptions.model;

import java.io.Serializable;
import java.util.Date;

/**
 * Subscription value object to store data on an individual subscription.
 * Every field needs a getter and setter so this object can be written to
 * and from files in json form.
 */
public class Subscription implements Serializable {

    // TODO: add categories and time between charges
    private String name;
    private double cost;
    private Date startDate;
    private String note;

    /**
     * Create and initialize all the values of this subscription.
     * @param name the name of the subscription
     * @param cost how much it costs
     * @param startDate when the subscription first started
     * @param note any miscellaneous notes
     */
    public Subscription(String name, double cost, Date startDate, String note) {
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.note = note;
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
}
