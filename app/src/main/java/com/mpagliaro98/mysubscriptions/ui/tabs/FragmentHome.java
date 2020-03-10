package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.CreateSubscriptionActivity;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionView;
import java.io.IOException;
import java.util.Comparator;

/**
 * A fragment containing the view for the home tab. Implements the OnDataListenerReceived
 * interface so we can get data from the Create activity and properly send it to the
 * model.
 */
public class FragmentHome extends Fragment implements MainActivity.OnDataListenerReceived {

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
        MainActivity mainActivity = (MainActivity)getActivity();
        mainActivity.checkIncomingData(this);
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
        final View root = inflater.inflate(R.layout.fragment_home_tab, container, false);
        updateSubList(root);

        // Add a listener to the search bar that will filter the list each time it's used
        TextView searchBar = root.findViewById(R.id.home_search);
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                model.filterList(s);
                updateSubList(root);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Add a listener to the sort list to sort the list when each item is selected
        Spinner sortDropdown = root.findViewById(R.id.home_sort_list);
        sortDropdown.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String sortOption = (String)parent.getSelectedItem();
                TextView searchBar = root.findViewById(R.id.home_search);
                String searchText = searchBar.getText().toString();
                // For the default option, just sort by ID
                if (sortOption.equals(getString(R.string.array_sort_default))) {
                    model.sortList(new Comparator<Subscription>() {
                        @Override
                        public int compare(Subscription o1, Subscription o2) {
                            return o1.getId() < o2.getId() ? -1 : 1;
                        }
                    }, searchText);
                }
                // Sort by names
                else if (sortOption.equals(getString(R.string.array_sort_name))) {
                    model.sortList(new Comparator<Subscription>() {
                        @Override
                        public int compare(Subscription o1, Subscription o2) {
                            return o1.getName().compareToIgnoreCase(o2.getName());
                        }
                    }, searchText);
                }
                // Sort by cost, if cost is equal then sort by name
                else if (sortOption.equals(getString(R.string.array_sort_cost))) {
                    model.sortList(new Comparator<Subscription>() {
                        @Override
                        public int compare(Subscription o1, Subscription o2) {
                            if (o1.getCost() == o2.getCost()) {
                                return o1.getName().compareToIgnoreCase(o2.getName());
                            } else {
                                return o1.getCost() < o2.getCost() ? -1 : 1;
                            }
                        }
                    }, searchText);
                }
                // Sort by next payment date
                else if (sortOption.equals(getString(R.string.array_sort_next))) {
                    model.sortList(new Comparator<Subscription>() {
                        @Override
                        public int compare(Subscription o1, Subscription o2) {
                            return o1.getNextPaymentDate().compareTo(o2.getNextPaymentDate());
                        }
                    }, searchText);
                }
                // Sort by category
                else if (sortOption.equals(getString(R.string.array_sort_category))) {
                    // TODO implement when categories are added
                    Snackbar.make(root, R.string.unimplemented, Snackbar.LENGTH_SHORT).show();
                }
                updateSubList(root);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return root;
    }

    /**
     * Update the UI component that displays a list of every subscription.
     * @param view the current view to display to
     */
    private void updateSubList(View view) {
        LinearLayout linearLayout = view.findViewById(R.id.home_linear_layout);
        linearLayout.removeAllViewsInLayout();
        for (int i = 0; i < model.numSubscriptionsVisible(); i++) {
            final Subscription sub = model.getSubscription(i);
            final SubscriptionView subView = new SubscriptionView(getContext(), sub);
            subView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), CreateSubscriptionActivity.class);
                    intent.putExtra(CreateSubscriptionActivity.PAGE_TYPE_MESSAGE,
                            CreateSubscriptionActivity.PAGE_TYPE.VIEW);
                    intent.putExtra(CreateSubscriptionActivity.VIEW_SUB_MESSAGE,
                                    sub);
                    intent.putExtra(CreateSubscriptionActivity.SUB_ID_MESSAGE,
                                    sub.getId());
                    startActivity(intent);
                }
            });
            linearLayout.addView(subView);
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
    public void onDataReceived(Subscription subscription, MainActivity.INCOMING_TYPE type,
                               Integer subIndex) {
        if (type == MainActivity.INCOMING_TYPE.CREATE) {
            model.addSubscription(subscription);
        } else if (type == MainActivity.INCOMING_TYPE.EDIT) {
            model.updateSubscription(subscription, subIndex);
        } else if (type == MainActivity.INCOMING_TYPE.DELETE) {
            model.deleteSubscription(subIndex);
        }
        try {
            model.saveToFile(getContext());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}