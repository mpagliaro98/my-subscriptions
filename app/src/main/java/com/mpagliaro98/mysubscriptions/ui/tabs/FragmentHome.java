package com.mpagliaro98.mysubscriptions.ui.tabs;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.snackbar.Snackbar;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.SharedViewModel;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.CreateSubscriptionActivity;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;
import com.mpagliaro98.mysubscriptions.ui.components.SubscriptionView;
import com.mpagliaro98.mysubscriptions.ui.interfaces.OnDataListenerReceived;
import com.mpagliaro98.mysubscriptions.ui.interfaces.SavedStateCompatible;
import java.io.IOException;
import java.util.Comparator;

/**
 * A fragment containing the view for the home tab. Implements the OnDataListenerReceived
 * interface so we can get data from the Create activity and properly send it to the
 * model.
 */
public class FragmentHome extends Fragment implements OnDataListenerReceived, SavedStateCompatible {

    // The model shared by the three main tabs
    private SharedViewModel model;
    // Any saved state from previously in the application to apply when loading the view
    private Bundle savedState;
    // Flags to tell the view to display an error message if something went wrong early on
    private boolean errorFlag = false;
    private boolean noMemoryError = false;

    // Keys for the saved state of the home fragment when returning
    public static final String SAVED_STATE_SCROLL_MESSAGE = "com.mpagliaro98.mysubscriptions.H_SAVED_SCROLL";
    public static final String SAVED_STATE_SEARCH_MESSAGE = "com.mpagliaro98.mysubscriptions.H_SAVED_SEARCH";
    public static final String SAVED_STATE_SORT_MESSAGE = "com.mpagliaro98.mysubscriptions.H_SAVED_SORT";

    //////////////////////////////////////////////////////////////////////////////////////////
    // PUBLIC METHODS ////////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

    /**
     * Create this fragment object and set its saved state bundle to be used later.
     * @param savedState bundle of saved state
     */
    public FragmentHome(Bundle savedState) {
        super();
        this.savedState = savedState;
    }

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
            Context context = getContext();
            assert context != null;
            model.loadFromFile(context);
            int numUpdated = model.updateSubscriptionDates();
            if (numUpdated > 0) {
                model.saveToFile(getContext());
            }
        } catch(IOException e) {
            e.printStackTrace();
            errorFlag = true;
            if (e.getMessage() != null && e.getMessage().equals(getString(R.string.no_memory_exception)))
                noMemoryError = true;
        } catch(Exception e) {
            deleteDataDialog();
        }

        // Set this fragment as the data listener for the tab activity
        MainActivity mainActivity = (MainActivity)getActivity();
        assert mainActivity != null;
        mainActivity.checkIncomingData(this);
    }

    /**
     * Creates the root view for this fragment.
     * @param inflater inflater to instantiate the xml view into an object
     * @param container the group that will serve as the base for the view
     * @param savedInstanceState any saved state needed
     * @return the view that displays this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View root = inflater.inflate(R.layout.fragment_home_tab, container, false);
        if (errorFlag) {
            FragmentActivity activity = getActivity();
            assert activity != null;
            if (noMemoryError)
                showErrorSnackbar(activity.findViewById(android.R.id.content), getString(R.string.no_memory_exception));
            else
                showErrorSnackbar(activity.findViewById(android.R.id.content), getString(R.string.home_snackbar_ioexception));
            errorFlag = false;
            noMemoryError = false;
            return root;
        }
        updateSubList(root);

        // Add a listener to the search bar that will filter the list each time it's used
        addSearchBarListener((TextView)root.findViewById(R.id.home_search), root);

        // Add a listener to the sort list to sort the list when each item is selected
        addSortDropdownListener((Spinner)root.findViewById(R.id.home_sort_list), root);

        // Apply the values from the saved state to the page
        if (savedState != null) {
            applySavedState(savedState, root);
        }

        return root;
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
            Context context = getContext();
            assert context != null;
            model.saveToFile(context);
        } catch (IOException e) {
            errorFlag = true;
            if (e.getMessage() != null && e.getMessage().equals(getString(R.string.no_memory_exception)))
                noMemoryError = true;
        }
    }

    /**
     * Populate a given bundle with values pertaining to how this fragment is set. For
     * FragmentHome, the scroll amount, search bar text, and sort dropdown selections are
     * saved, so they can be reset to the saved values when returning to this fragment.
     * The public keys at the top of this fragment are used to index the saved values.
     * @param bundle the bundle to place the saved items in
     */
    @Override
    public void fillBundleWithSavedState(Bundle bundle) {
        View view = getView();
        assert view != null;
        ScrollView scrollView = view.findViewById(R.id.home_scroll_view);
        bundle.putInt(SAVED_STATE_SCROLL_MESSAGE, scrollView.getScrollY());
        TextView searchBar = view.findViewById(R.id.home_search);
        bundle.putString(SAVED_STATE_SEARCH_MESSAGE, searchBar.getText().toString());
        Spinner sortDropdown = view.findViewById(R.id.home_sort_list);
        bundle.putInt(SAVED_STATE_SORT_MESSAGE, sortDropdown.getSelectedItemPosition());
    }

    /**
     * Given a bundle of saved state, extract the values that were saved to it previously
     * and re-apply them to this view. For this tab, it will re-apply the search bar text,
     * the sort dropdown selection, and the Y scroll distance.
     * @param savedState bundle of saved state, must not be null
     * @param root the root view of this tab
     */
    @Override
    public void applySavedState(@NonNull final Bundle savedState, View root) {
        if (savedState.containsKey(SAVED_STATE_SORT_MESSAGE)) {
            Spinner sortDropdown = root.findViewById(R.id.home_sort_list);
            sortDropdown.setSelection(savedState.getInt(SAVED_STATE_SORT_MESSAGE));
        }
        if (savedState.containsKey(SAVED_STATE_SEARCH_MESSAGE)) {
            TextView searchBar = root.findViewById(R.id.home_search);
            searchBar.setText(savedState.getString(SAVED_STATE_SEARCH_MESSAGE));
        }
        if (savedState.containsKey(SAVED_STATE_SCROLL_MESSAGE)) {
            final ScrollView scrollView = root.findViewById(R.id.home_scroll_view);
            scrollView.post(new Runnable() {
                @Override
                public void run() {
                    scrollView.scrollTo(0, savedState.getInt(SAVED_STATE_SCROLL_MESSAGE));
                }
            });
        }
    }

    //////////////////////////////////////////////////////////////////////////////////////////
    // PRIVATE METHODS ///////////////////////////////////////////////////////////////////////
    //////////////////////////////////////////////////////////////////////////////////////////

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
                    Bundle savedState = null;
                    if (getActivity() != null)
                        savedState = ((MainActivity)getActivity()).gatherSavedState();
                    Intent intent = CreateSubscriptionActivity.buildGeneralCreateIntent(getContext(),
                            CreateSubscriptionActivity.PAGE_TYPE.VIEW, sub, sub.getId(), savedState);
                    startActivity(intent);
                }
            });
            linearLayout.addView(subView);
        }
    }

    /**
     * Display a dialog box informing the user their data is corrupt and saying that
     * it needs to be reset, which when agreed to resets the application data.
     */
    private void deleteDataDialog() {
        final Context context = getContext();
        assert context != null;
        new AlertDialog.Builder(context)
                .setTitle(context.getString(R.string.delete_dialog_title))
                .setMessage(context.getString(R.string.delete_dialog_content))
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            model.deleteData(context);
                        } catch (IOException e) {
                            if (e.getMessage() != null && e.getMessage().equals(getString(R.string.no_memory_exception)))
                                showErrorSnackbar(getView(), getString(R.string.no_memory_exception));
                            else
                                showErrorSnackbar(getView(), getString(R.string.home_snackbar_ioexception));
                        }
                    }
                }).show();
    }

    /**
     * Add a listener to the search bar of this tab, which will filter the list of
     * subscriptions whenever the text entered into it changes.
     * @param searchBar the search bar view
     * @param root the root view of this tab
     */
    private void addSearchBarListener(TextView searchBar, final View root) {
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
    }

    /**
     * Add a listener to the sorting dropdown of this tab, which will sort the list of
     * subscriptions whenever its selection is changed.
     * @param sortDropdown the sort dropdown spinner
     * @param root the root view of this tab
     */
    private void addSortDropdownListener(Spinner sortDropdown, final View root) {
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
                            if (o1.getId() == o2.getId())
                                return 0;
                            else
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
                // Sort by next payment date, if equal then sort by name
                else if (sortOption.equals(getString(R.string.array_sort_next))) {
                    model.sortList(new Comparator<Subscription>() {
                        @Override
                        public int compare(Subscription o1, Subscription o2) {
                            if (o1.getNextPaymentDate().equals(o2.getNextPaymentDate())) {
                                return o1.getName().compareToIgnoreCase(o2.getName());
                            } else {
                                return o1.getNextPaymentDate().compareTo(o2.getNextPaymentDate());
                            }
                        }
                    }, searchText);
                }
                // Sort by category, which sorts by category name, then subscription name
                else if (sortOption.equals(getString(R.string.array_sort_category))) {
                    model.sortList(new Comparator<Subscription>() {
                        @Override
                        public int compare(Subscription o1, Subscription o2) {
                            if (o1.getCategory().getName().equals(o2.getCategory().getName())) {
                                return o1.getName().compareToIgnoreCase(o2.getName());
                            } else {
                                return o1.getCategory().getName().compareToIgnoreCase(o2.getCategory().getName());
                            }
                        }
                    }, searchText);
                }
                updateSubList(root);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Display an error message along the bottom of the screen saying an error occurred, and give
     * the option to reload the current view. This should be called when an IOException occurs
     * from accessing subscription data.
     * @param view the view to display the Snackbar message in
     * @param errorMessage the string to display on the Snackbar
     */
    private void showErrorSnackbar(View view, String errorMessage) {
        Snackbar ioExceptionBar = Snackbar.make(view, errorMessage, Snackbar.LENGTH_INDEFINITE);
        ioExceptionBar.setAction(R.string.retry, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = MainActivity.buildGeneralMainIntent(getContext(), null,
                        null, -1, null);
                startActivity(intent);
            }
        });
        ioExceptionBar.show();
    }
}