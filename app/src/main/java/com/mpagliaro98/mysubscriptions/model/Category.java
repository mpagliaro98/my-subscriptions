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

    /**
     * Constructor for a category, takes a color and a name.
     * @param color A color as an int (must be defined in colors.xml)
     * @param name The name of this subscription
     */
    public Category(int color, String name) {
        this.color = color;
        this.name = name;
    }

    /**
     * Get the color of this category.
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
     * Two categories are equal if their colors and names are equivalent.
     * @param obj The object to compare to this category
     * @return true if they are equal, false otherwise
     */
    @Override
    public boolean equals(@Nullable Object obj) {
        if (this == obj) {
            return true;
        } else if (obj instanceof Category) {
            Category cat2 = (Category)obj;
            if (this.name.equals(cat2.name) && this.color == cat2.color) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}
