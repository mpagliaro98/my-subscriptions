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
    // The list where subscriptions are stored
    private ArrayList<Subscription> subscriptionList = new ArrayList<>();
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
     * Get a subscription object from the list.
     * @param index index of the subscription to get
     * @return a subscription object
     */
    public Subscription getSubscription(int index) {
        return subscriptionList.get(index);
    }

    /**
     * Add a subscription to the end of the list.
     * @param subscription a subscription object to add to the list
     */
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
        subscriptionList.clear();
        Gson gson = new Gson();
        FileInputStream fis = context.openFileInput(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(inputStreamReader);

        // Read each line, convert it to a subscription object from json, then put it in the list
        String line = reader.readLine();
        while (line != null) {
            Subscription subscription = gson.fromJson(line, Subscription.class);
            addSubscription(subscription);
            line = reader.readLine();
        }
        reader.close();
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
        for (int i = 0; i < numSubscriptions(); i++) {
            Subscription subscription = getSubscription(i);
            String line = gson.toJson(subscription) + "\n";
            fos.write(line.getBytes());
        }
        fos.close();
    }
}