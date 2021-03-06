package com.mpagliaro98.mysubscriptions.model;

import androidx.lifecycle.ViewModel;
import android.content.Context;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import com.google.gson.Gson;
import com.mpagliaro98.mysubscriptions.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

/**
 * The model shared between each fragment, keeping track of the data each page of
 * the application needs.
 */
public class SharedViewModel extends ViewModel {

    // An underlying hierarchy of lists is kept to handle the subscription list
    // The full list has all subscriptions in it, always ordered by ID
    private ArrayList<Subscription> fullSubscriptionList = new ArrayList<>();
    // Re-orderable list always has all the elements the full list has, but can be re-ordered
    // The viewable list takes what re-orderable list has and filters it before displaying
    private ArrayList<Subscription> reorderableFullSubscriptionList = fullSubscriptionList;
    // The viewable list is the top level list, its contents are shown in the UI
    private ArrayList<Subscription> viewableSubscriptionList = fullSubscriptionList;
    // The filename the data is kept in
    static final String SUBSCRIPTIONS_FILENAME = "subscriptions.dat";

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

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
     * Get the subscriptions that have a next payment date due on the given date, and return
     * a list containing these subscriptions. It will check any number of payment dates in the
     * future, not just the immediate next payment date.
     * @param date the date to check for subscriptions on
     * @return a list of subscription objects due on the given date
     */
    public List<Subscription> getSubsDueOnDate(Date date) {
        List<Subscription> subsDueList = new ArrayList<>();
        for (Subscription sub : fullSubscriptionList) {
            if (sub.getNextPaymentList() != null) {
                for (Date paymentDate : sub.getNextPaymentList()) {
                    if (paymentDate.equals(date)) {
                        subsDueList.add(sub);
                        break;
                    }
                }
            }
        }
        return subsDueList;
    }

    /**
     * Iterate through every subscription in the model and regenerate the relevant date info
     * for those whose next payment dates have passed. A new ZeroTimeCalendar instance is
     * created and used for this method.
     * @return the number of subscriptions updated
     */
    public int updateSubscriptionDates() {
        return updateSubscriptionDates(new ZeroTimeCalendar());
    }

    /**
     * Iterate through every subscription in the model and regenerate the relevant date info
     * for those whose next payment dates have passed.
     * @param zeroTimeCalendar a calendar of today's date with the time set to 0:00:00
     * @return the number of subscriptions updated
     */
    int updateSubscriptionDates(ZeroTimeCalendar zeroTimeCalendar) {
        // Get today's date at 0:00:00 (so it matches with dates in subscriptions)
        Date today = zeroTimeCalendar.getCurrentDate();

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
        // Don't do anything if the internal file doesn't exist
        File file = new File(context.getFilesDir(), SUBSCRIPTIONS_FILENAME);
        if (!file.exists()) {
            return;
        }

        // Clear the subscriptions so we can populate the list, then make the reader
        fullSubscriptionList.clear();
        Gson gson = new Gson();
        FileInputStream fis = context.openFileInput(SUBSCRIPTIONS_FILENAME);
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
     * @throws IOException thrown if something goes wrong writing to the file, or if not enough
     *                     memory is available to write the file
     */
    public void saveToFile(Context context) throws IOException {
        // Estimate how much storage we will need, throw an error if there's not enough
        if (getAvailableMemory() <= estimateNeededStorage()) {
            throw new IOException(context.getResources().getString(R.string.no_memory_exception));
        }

        // Refresh the file so we write to it from scratch
        Gson gson = new Gson();
        File file = new File(context.getFilesDir(), SUBSCRIPTIONS_FILENAME);
        if (file.exists()) {
            if (!file.delete()) {
                throw new IOException();
            }
        }
        if (!file.createNewFile()) {
            throw new IOException();
        }

        // Convert each subscription to json, then add it to the file line by line
        FileOutputStream fos = context.openFileOutput(SUBSCRIPTIONS_FILENAME, Context.MODE_PRIVATE);
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

    /**
     * Gets the available amount of memory in the system in bytes.
     * @return the amount of memory the system has available
     */
    private long getAvailableMemory() {
        File path = Environment.getDataDirectory();
        StatFs statFs = new StatFs(path.getPath());
        long blockSize, availableBlocks;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            blockSize = statFs.getBlockSizeLong();
            availableBlocks = statFs.getAvailableBlocksLong();
        } else {
            blockSize = statFs.getBlockSize();
            availableBlocks = statFs.getAvailableBlocks();
        }
        return availableBlocks * blockSize;
    }

    /**
     * Estimates how much storage in bytes the system will need to be able to save
     * the subscriptions file. This estimate uses 500 times the number of subscriptions,
     * so if there are 8 subscriptions saved, it'll estimate 4000 bytes, or about 4 megabytes.
     * In practice, the actual space needed will be around half of that, but this is
     * made to be a large enough estimation to account for lots of extra data, like long
     * notes on each subscription. If there's no subscriptions, it defaults to 10 bytes.
     * @return the estimate of how much space is needed in bytes
     */
    private long estimateNeededStorage() {
        if (fullSubscriptionList.size() == 0){
            return 10;
        } else {
            return fullSubscriptionList.size() * 500;
        }
    }
}