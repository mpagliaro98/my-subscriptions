package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.CreateSubscriptionActivity;
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
        model = new ViewModelProvider(this).get(SharedViewModel.class);

        // Populate the model by loading subscriptions from the file
        try {
            model.loadFromFile(getContext());
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Set this fragment as the data listener for the tab activity
        HomeTabActivity homeTabActivity = (HomeTabActivity)getActivity();
        homeTabActivity.checkIncomingData(this);
    }

    /**
     * Creates the root view for this fragment.
     * @param inflater
     * @param container
     * @param savedInstanceState any saved state needed
     * @return the view that displays this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home_tab, container, false);
        updateSubList(root);
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
            final Subscription sub = model.getSubscription(i);
            final int subIndex = i;
            textView.setText(sub.getName());
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), CreateSubscriptionActivity.class);
                    intent.putExtra(CreateSubscriptionActivity.PAGE_TYPE_MESSAGE,
                            CreateSubscriptionActivity.PAGE_TYPE.VIEW);
                    intent.putExtra(CreateSubscriptionActivity.VIEW_SUB_MESSAGE,
                                    sub);
                    intent.putExtra(CreateSubscriptionActivity.SUB_ID_MESSAGE,
                                    subIndex);
                    startActivity(intent);
                }
            });
            linearLayout.addView(textView);
        }
    }

    /**
     * Receive data from another activity, passed to here through this fragment's
     * parent activity. In this case, the data is a subscription object modified in
     * a separate activity, which we will perform an operation on depending on what
     * action should be taken.
     * @param subscription the new subscription object
     * @param type the action to take on the incoming data, either CREATE, EDIT, or DELETE
     * @param subIndex if required, the index in the list of the item to modify
     */
    @Override
    public void onDataReceived(Subscription subscription, HomeTabActivity.INCOMING_TYPE type,
                               Integer subIndex) {
        if (type == HomeTabActivity.INCOMING_TYPE.CREATE) {
            model.addSubscription(subscription);
        } else if (type == HomeTabActivity.INCOMING_TYPE.EDIT) {
            model.updateSubscription(subscription, subIndex);
        } else if (type == HomeTabActivity.INCOMING_TYPE.DELETE) {
            model.deleteSubscription(subIndex);
        }
        try {
            model.saveToFile(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}