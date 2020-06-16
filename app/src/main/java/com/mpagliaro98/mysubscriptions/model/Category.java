package com.mpagliaro98.mysubscriptions.model;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.Serializable;

/**
 * The class for creating various categories to cluster subscriptions into different
 * groups. To add a new category to this application, add a new object declaration
 * to categoryList in CreateSubscriptionActivity.
 */
public class Category implements Serializable {

    private int color;
    private String name;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Constructor for a category, takes a color and a name.
     * @param color A color as an int (between 0x000000 and 0xFFFFFF)
     * @param name The name of this subscription
     */
    public Category(int color, String name) {
        this.color = color;
        this.name = name;
    }

    /**
     * Get the color of this category. This will be the actual value of the color and not
     * a resource value.
     * @return The color as an int
     */
    public int getColor() {
        return color;
    }

    /**
     * Get the name of this category.
     * @return The name as a string
     */
    public String getName() {
        return name;
    }

    /**
     * Make a string representation of this category, which is just the name.
     * @return The name of this category
     */
    @NonNull
    @Override
    public String toString() {
        return name;
    }

    /**
     * Two categories are equal if their colors are the same and their names are the
     * same regardless of case.
     * @param obj The object to compare to this category
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Category) {
            Category cat2 = (Category)obj;
            return this.name.equalsIgnoreCase(cat2.name) && this.color == cat2.color;
        } else {
            return false;
        }
    }

    /**
     * Return the hashcode of this category to use in lookups, which is just the
     * integer value of its color.
     * @return the hashcode as an integer
     */
    @Override
    public int hashCode() {
        return this.color;
    }
}
