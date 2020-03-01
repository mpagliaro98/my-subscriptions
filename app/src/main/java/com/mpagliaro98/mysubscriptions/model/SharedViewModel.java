package com.mpagliaro98.mysubscriptions.model;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Transformations;
import android.arch.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;

/**
 * The model shared between each fragment, keeping track of the data each page of
 * the application needs.
 */
public class SharedViewModel extends ViewModel {

    // Test value for keeping a mutable string that can change based on tab
    private MutableLiveData<String> mName = new MutableLiveData<>();
    private LiveData<String> mText = Transformations.map(mName, new Function<String, String>() {
        @Override
        public String apply(String input) {
            return "Currently on tab: " + input;
        }
    });
    // The list where subscriptions are stored
    private ArrayList<Subscription> subscriptionList = new ArrayList<>();

    /**
     * Set the value that goes into the mutable text.
     * @param name string to add
     */
    public void setName(String name) {
        mName.setValue(name);
    }

    /**
     * Get the mutable text.
     * @return a string live data object
     */
    public LiveData<String> getText() {
        return mText;
    }

    /**
     * Get a subscription object from the list.
     * @param index index of the subscription to get
     * @return a subscription object
     */
    public Subscription getSubscription(int index) {
        return subscriptionList.get(index);
    }

    /**
     * Add a subscription to the end of the list.
     * @param name name of the subscription
     * @param cost cost of the subscription
     * @param startDate the date the subscription was started
     * @param note an optional note for the subscription
     */
    public void addSubscription(String name, Currency cost, Date startDate, String note) {
        subscriptionList.add(new Subscription(name, cost, startDate, note));
    }

    /**
     * Get the number of subscriptions currently saved.
     * @return the number of subscriptions as an int
     */
    public int numSubscriptions() {
        return subscriptionList.size();
    }
}