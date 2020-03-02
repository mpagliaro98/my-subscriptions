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

/**
 * A fragment containing the view for the home tab.
 */
public class FragmentHome extends Fragment implements HomeTabActivity.OnDataListenerReceived {

    private SharedViewModel model;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = ViewModelProviders.of(this).get(SharedViewModel.class);
        model.setName("Home Tab");

        HomeTabActivity homeTabActivity = (HomeTabActivity)getActivity();
        homeTabActivity.setDataListener(this);
    }

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

    private void updateSubList(View view) {
        LinearLayout linearLayout = view.findViewById(R.id.home_linear_layout);
        //model.addSubscription("sub1", null, null, "note");
        //model.addSubscription("sub2", null, null, "note");
        for (int i = 0; i < model.numSubscriptions(); i++) {
            final TextView textView = new TextView(getActivity());
            textView.setText(model.getSubscription(i).getName());
            linearLayout.addView(textView);
        }
    }

    @Override
    public void onDataReceived(Subscription subscription) {
        model.addSubscription(subscription);
    }
}