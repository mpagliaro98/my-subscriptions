package com.mpagliaro98.mysubscriptions.model;

import androidx.arch.core.util.Function;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import android.content.Context;
import com.google.gson.Gson;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

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
    // The list where subscriptions are stored, viewable is what will be sent to the view
    private ArrayList<Subscription> viewableSubscriptionList = new ArrayList<>();
    private ArrayList<Subscription> fullSubscriptionList = new ArrayList<>();
    // The filename the data is kept in
    private static final String filename = "subscriptions.dat";

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
     * Get a subscription object from the list. This will fetch from the list of
     * subscriptions currently viewable (if the list is filtered by search or sort).
     * @param index index of the subscription to get
     * @return a subscription object
     */
    public Subscription getSubscription(int index) {
        return viewableSubscriptionList.get(index);
    }

    /**
     * Add a subscription to the end of the list.
     * @param subscription a subscription object to add to the list
     */
    public void addSubscription(Subscription subscription) {
        // For each new sub, set its ID to the next available ID.
        subscription.setId(fullSubscriptionList.size());
        fullSubscriptionList.add(subscription);
    }

    /**
     * Update a subscription at a given index by replacing it with a new instance.
     * @param subscription the new subscription to put in the list
     * @param index the index of the subscription to replace
     */
    public void updateSubscription(Subscription subscription, int index) {
        fullSubscriptionList.set(index, subscription);
    }

    /**
     * Remove a subscription from the list at a given index.
     * @param index the index of the subscription to remove
     */
    public void deleteSubscription(int index) {
        fullSubscriptionList.remove(index);
        updateAllSubIds();
    }

    /**
     * Re-distribute IDs for every subscription, sequentially from 0 in the order of
     * the full subscription list. This should be done if a subscription is removed,
     * leaving a gap in the subscription IDs.
     */
    private void updateAllSubIds() {
        for (int i = 0; i < numSubscriptionsTotal(); i++) {
            Subscription sub = fullSubscriptionList.get(i);
            sub.setId(i);
        }
    }

    /**
     * Get the number of subscriptions currently viewable on the page.
     * @return the number of subscriptions as an int
     */
    public int numSubscriptionsVisible() {
        return viewableSubscriptionList.size();
    }

    /**
     * Get the total number of subscriptions, regardless of what is visible.
     * @return the total number of subscriptions as an int
     */
    public int numSubscriptionsTotal() {
        return fullSubscriptionList.size();
    }

    /**
     * Filter the viewable list of subscriptions based on some string input. The given
     * string will filter the list to include only subscriptions that contain that
     * string in their name.
     * @param searchText What text should be included in each subscription's name
     */
    public void filterList(CharSequence searchText) {
        ArrayList<Subscription> filteredList = new ArrayList<>();
        for (Subscription sub : fullSubscriptionList) {
            if (sub.getName().contains(searchText)) {
                filteredList.add(sub);
            }
        }
        viewableSubscriptionList = filteredList;
    }

    /**
     * Reads in the internal storage file to populate our list of subscriptions.
     * @param context the current context of the application
     * @throws IOException thrown if something goes wrong in reading the file
     */
    public void loadFromFile(Context context) throws IOException {
        // TODO add more IO checks, like making sure there's enough storage before writing
        // TODO add a snackbar to display IOExceptions if they occur
        // Don't do anything if the internal file doesn't exist
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) {
            return;
        }

        // Clear the subscriptions so we can populate the list, then make the reader
        fullSubscriptionList.clear();
        Gson gson = new Gson();
        FileInputStream fis = context.openFileInput(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        // Read each line, convert it to a subscription object from json, then put it in the list
        String line = reader.readLine();
        int id = 0;
        while (line != null) {
            Subscription subscription = gson.fromJson(line, Subscription.class);
            subscription.generateNextPaymentDate(context.getResources());
            subscription.setId(id);
            fullSubscriptionList.add(subscription);
            line = reader.readLine();
            id++;
        }
        reader.close();
        viewableSubscriptionList = fullSubscriptionList;
    }

    /**
     * Save every subscription currently in the list to a file in internal storage.
     * @param context the current context of the application
     * @throws IOException thrown if something goes wrong writing to the file
     */
    public void saveToFile(Context context) throws IOException {
        // Refresh the file so we write to it from scratch
        Gson gson = new Gson();
        File file = new File(context.getFilesDir(), filename);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException();
            }
        }
        if (!file.createNewFile()) {
            throw new IOException();
        }

        // Convert each subscription to json, then add it to the file line by line
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        for (int i = 0; i < fullSubscriptionList.size(); i++) {
            Subscription subscription = fullSubscriptionList.get(i);
            String line = gson.toJson(subscription) + "\n";
            fos.write(line.getBytes());
        }
        fos.close();
    }
}