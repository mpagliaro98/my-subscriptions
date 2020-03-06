package com.mpagliaro98.mysubscriptions.model;

import android.app.Application;
import android.content.Context;
import java.lang.ref.WeakReference;

/**
 * This class was made as a work-around to be able to access user-defined resource
 * strings in classes that aren't related to activities.
 */
public class ContextFetcher extends Application {

    // Uses a weak reference to avoid memory leaks
    private static WeakReference<Context> context;

    /**
     * When this application is created, set the context so it can
     * be accessed anywhere.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        context = new WeakReference<Context>(this);
    }

    /**
     * Get the application context.
     * @return the application context
     */
    public static Context getContext() {
        return context.get();
    }
}
