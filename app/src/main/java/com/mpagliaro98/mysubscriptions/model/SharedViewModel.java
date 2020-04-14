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
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

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
    // An underlying hierarchy of lists is kept to handle the subscription list
    // The full list has all subscriptions in it, always ordered by ID
    private ArrayList<Subscription> fullSubscriptionList = new ArrayList<>();
    // Re-orderable list always has all the elements the full list has, but can be re-ordered
    // The viewable list takes what re-orderable list has and filters it before displaying
    private ArrayList<Subscription> reorderableFullSubscriptionList = fullSubscriptionList;
    // The viewable list is the top level list, its contents are shown in the UI
    private ArrayList<Subscription> viewableSubscriptionList = fullSubscriptionList;
    // The filename the data is kept in
    private static final String filename = "subscriptions.dat";

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

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
     * Get the entire subscription list regardless of sorting or filtering.
     * @return a list of every subscription in ID order
     */
    public List<Subscription> getFullSubscriptionList() {
        return fullSubscriptionList;
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
        subscription.setId(index);
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
        String search = searchText.toString();
        ArrayList<Subscription> filteredList = new ArrayList<>();
        for (Subscription sub : reorderableFullSubscriptionList) {
            if (sub.getName().toLowerCase().contains(search.toLowerCase())) {
                filteredList.add(sub);
            }
        }
        viewableSubscriptionList = filteredList;
    }

    /**
     * Sort the underlying re-orderable list. This also requires the search text to be passed
     * in so we can update the viewable list with any search conditions as well.
     * @param comparator A comparator function that takes two subscriptions and returns -1 if
     *                   sub1 < sub2, 1 if sub1 > sub2, or 0 if they are equal
     * @param searchText The next in the search box
     */
    public void sortList(Comparator<Subscription> comparator, CharSequence searchText) {
        Collections.sort(reorderableFullSubscriptionList, comparator);
        filterList(searchText);
    }

    /**
     * Iterate through every subscription in the model and regenerate the relevant date info
     * for those whose next payment dates have passed.
     * @param zeroTimeCalendar a calendar of today's date with the time set to 0:00:00
     * @return the number of subscriptions updated
     */
    public int updateSubscriptionDates(Calendar zeroTimeCalendar) {
        // Get today's date at 0:00:00 (so it matches with dates in subscriptions)
        Date today = zeroTimeCalendar.getTime();

        // Iterate through every subscription and update ones whose payment dates have passed
        int numUpdated = 0;
        for (Subscription sub : fullSubscriptionList) {
            if (today.after(sub.getNextPaymentDate())) {
                sub.regenerateSubInfo(zeroTimeCalendar);
                updateSubscription(sub, sub.getId());
                numUpdated++;
            }
        }

        // Return how many subscriptions were updated
        return numUpdated;
    }

    /**
     * Reads in the internal storage file to populate our list of subscriptions.
     * @param context the current context of the application
     * @throws IOException thrown if something goes wrong in reading the file
     */
    public void loadFromFile(Context context) throws IOException {
        // TODO add more IO checks, like making sure there's enough storage before writing
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
            subscription.setId(id);
            fullSubscriptionList.add(subscription);
            line = reader.readLine();
            id++;
        }
        reader.close();
        viewableSubscriptionList = fullSubscriptionList;
        reorderableFullSubscriptionList = fullSubscriptionList;
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

    /**
     * Reset this application's data by clearing the current list and overwriting the
     * saved file.
     * @param context the current application context
     * @throws IOException thrown if something goes wrong accessing the save file
     */
    public void deleteData(Context context) throws IOException {
        fullSubscriptionList = new ArrayList<>();
        reorderableFullSubscriptionList = fullSubscriptionList;
        viewableSubscriptionList = fullSubscriptionList;
        saveToFile(context);
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

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
}