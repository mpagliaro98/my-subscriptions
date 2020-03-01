package com.mpagliaro98.mysubscriptions.model;

import java.util.Currency;
import java.util.Date;

/**
 * Subscription value object to store data on an individual subscription.
 */
public class Subscription {

    // TODO: add categories and time between charges
    private String name;
    private Currency cost;
    private Date startDate;
    private String note;

    public Subscription(String name, Currency cost, Date startDate, String note) {
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public Currency getCost() {
        return cost;
    }

    public Date getStartDate() {
        return startDate;
    }

    public String getNote() {
        return note;
    }
}
