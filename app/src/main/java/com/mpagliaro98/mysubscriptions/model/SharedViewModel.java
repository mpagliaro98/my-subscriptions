package com.mpagliaro98.mysubscriptions.model;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import android.os.Environment;
import android.util.JsonWriter;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Currency;
import java.util.Date;
import java.util.Scanner;

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
    public void addSubscription(String name, double cost, Date startDate, String note) {
        addSubscription(new Subscription(name, cost, startDate, note));
    }
    public void addSubscription(Subscription subscription) {
        subscriptionList.add(subscription);
    }

    /**
     * Get the number of subscriptions currently saved.
     * @return the number of subscriptions as an int
     */
    public int numSubscriptions() {
        return subscriptionList.size();
    }

    public void loadFromFile() throws IOException {
        Gson gson = new Gson();
        File dir = new File(Environment.getExternalStorageDirectory(), "Data");
        if (!dir.exists()) {
            boolean result = dir.mkdirs();
            if (!result) {
                throw new IOException();
            }
        }
        File file = new File(dir, "subscriptions.dat");
        if (!file.exists()) {
            return;
        }
        Scanner sc = new Scanner(file);
        while (!sc.hasNext()) {
            String line = sc.nextLine();
            Subscription subscription = gson.fromJson(line, Subscription.class);
            subscriptionList.add(subscription);
        }
        sc.close();
    }

    public void saveToFile() throws IOException {
        Gson gson = new Gson();
        File dir = new File(Environment.getExternalStorageDirectory(), "Data");
        if (!dir.exists()) {
            boolean result = dir.mkdirs();
            if (!result) {
                throw new IOException();
            }
        }
        File file = new File(dir, "subscriptions.dat");
        FileWriter writer = new FileWriter(file);
        writer.write("");
        for (int i = 0; i < numSubscriptions(); i++) {
            Subscription subscription = subscriptionList.get(i);
            String line = gson.toJson(subscription);
            writer.write(line + "\n");
        }
        writer.close();
    }
}