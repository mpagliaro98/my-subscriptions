package com.mpagliaro98.mysubscriptions.model;

import java.util.Date;

/**
 * Subscription value object to store data on an individual subscription.
 */
public class Subscription {

    // TODO: add categories and time between charges
    private String name;
    private double cost;
    private Date startDate;
    private String note;

    public Subscription(String name, double cost, Date startDate, String note) {
        this.name = name;
        this.cost = cost;
        this.startDate = startDate;
        this.note = note;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }
}
