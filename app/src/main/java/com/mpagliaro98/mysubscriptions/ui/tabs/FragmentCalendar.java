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
import com.mpagliaro98.mysubscriptions.ui.interfaces.SavedStateCompatible;

/**
 * A fragment containing the view for the calendar tab.
 */
public class FragmentCalendar extends Fragment implements SavedStateCompatible {

    // The model shared by the three main tabs
    private SharedViewModel model;
    // Any saved state from previously in the application to apply when loading the view
    private Bundle savedState;

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create this fragment object and set its saved state bundle to be used later.
     * @param savedState bundle of saved state
     */
    public FragmentCalendar(Bundle savedState) {
        this.savedState = savedState;
    }

    /**
     * Default empty constructor for this fragment.
     */
    public FragmentCalendar() {}

    /**
     * Initializes the model for the calendar tab.
     * @param savedInstanceState any saved state needed
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        model = new ViewModelProvider(this).get(SharedViewModel.class);
    }

    /**
     * Creates the root view for this fragment.
     * @param inflater inflater to instantiate the xml view into an object
     * @param container the group that will serve as the base for the view
     * @param savedInstanceState any saved state needed
     * @return the view that displays this fragment
     */
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_calendar_tab, container, false);
        return root;
    }

    /**
     * Populate a given bundle with values pertaining to how this fragment is set.
     * @param bundle the bundle to place the saved items in
     */
    @Override
    public void fillBundleWithSavedState(Bundle bundle) {

    }

    /**
     * Given a bundle of saved state, extract the values that were saved to it previously
     * and re-apply them to this view.
     * @param savedState bundle of saved state, must not be null
     * @param root the root view of this tab
     */
    @Override
    public void applySavedState(@NonNull final Bundle savedState, View root) {

    }
}