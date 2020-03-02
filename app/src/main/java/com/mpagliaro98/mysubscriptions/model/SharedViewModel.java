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

    public void loadFromFile(Context context) throws IOException {
        Gson gson = new Gson();
        File file = new File(context.getFilesDir(), filename);
        if (!file.exists()) {
            return;
        }
        FileInputStream fis = context.openFileInput(filename);
        InputStreamReader inputStreamReader = new InputStreamReader(fis);
        BufferedReader reader = new BufferedReader(inputStreamReader);
        String line = reader.readLine();
        while (line != null) {
            System.out.println("Reading: " + line);
            Subscription subscription = gson.fromJson(line, Subscription.class);
            subscriptionList.add(subscription);
            line = reader.readLine();
        }
        reader.close();
    }

    public void saveToFile(Context context) throws IOException {
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
        FileOutputStream fos = context.openFileOutput(filename, Context.MODE_PRIVATE);
        for (int i = 0; i < numSubscriptions(); i++) {
            Subscription subscription = subscriptionList.get(i);
            String line = gson.toJson(subscription) + "\n";
            System.out.println("Writing: " + line);
            fos.write(line.getBytes());
        }
        fos.close();
    }
}