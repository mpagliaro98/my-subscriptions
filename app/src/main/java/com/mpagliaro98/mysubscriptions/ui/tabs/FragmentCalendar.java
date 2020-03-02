package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;

/**
 * A fragment containing the view for the calendar tab.
 */
public class FragmentCalendar extends Fragment {

    // The model shared by the three main tabs
    private SharedViewModel model;

    /**
     * Initializes the model for the calendar tab.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(this).get(SharedViewModel.class);
        model.setName("Calendar Tab");
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
        View root = inflater.inflate(R.layout.fragment_calendar_tab, container, false);
        final TextView textView = root.findViewById(R.id.section_label);
        model.getText().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(@Nullable String s) {
                textView.setText(s);
            }
        });
        return root;
    }
}