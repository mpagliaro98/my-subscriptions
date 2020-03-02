package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.HomeTabActivity;

import java.io.IOException;

/**
 * A fragment containing the view for the home tab. Implements the OnDataListenerReceived
 * interface so we can get data from the Create activity and properly send it to the
 * model.
 */
public class FragmentHome extends Fragment implements HomeTabActivity.OnDataListenerReceived {

    // The model shared by the three main tabs
    private SharedViewModel model;

    /**
     * Initializes the model for the home tab.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(SharedViewModel.class);
        model.setName("Home Tab");

        // Populate the model by loading subscriptions from the file
        try {
            model.loadFromFile(getContext());
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Set this fragment as the data listener for the tab activity
        HomeTabActivity homeTabActivity = (HomeTabActivity)getActivity();
        homeTabActivity.setDataListener(this);
    }

    /**
     * Creates the root view for this fragment.
     * @param inflater
     * @param container
     * @param savedInstanceState any saved state needed
     * @return the view that displays this fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home_tab, container, false);

        updateSubList(root);

        final TextView textView = root.findViewById(R.id.section_label);
        model.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }

    /**
     * Update the UI component that displays a list of every subscription.
     * @param view the current view to display to
     */
    private void updateSubList(View view) {
        LinearLayout linearLayout = view.findViewById(R.id.home_linear_layout);
        for (int i = 0; i < model.numSubscriptions(); i++) {
            final TextView textView = new TextView(getActivity());
            textView.setText(model.getSubscription(i).getName());
            linearLayout.addView(textView);
        }
    }

    /**
     * Receive data from another activity, passed to here through this fragment's
     * parent activity. In this case, the data is a subscription object created in
     * a separate activity, which we will add to the model and update the internal
     * storage file.
     * @param subscription the new subscription object
     */
    @Override
    public void onDataReceived(Subscription subscription) {
        model.addSubscription(subscription);
        try {
            model.saveToFile(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}