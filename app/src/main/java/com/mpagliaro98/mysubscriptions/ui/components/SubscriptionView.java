package com.mpagliaro98.mysubscriptions.ui.components;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.mpagliaro98.mysubscriptions.R;
import com.mpagliaro98.mysubscriptions.model.Subscription;
import java.text.SimpleDateFormat;
import java.util.Locale;

/**
 * A custom UI component for displaying Subscriptions a few relevant bits of data on them.
 */
public class SubscriptionView extends LinearLayout {

    private Subscription subscription;
    private TextView textName;
    private TextView textCost;
    private TextView textStartDate;

    /**
     * Build this view off of a LinearLayout so we can utilize some of its properties,
     * then initialize the text in the view.
     * @param context the current application context
     * @param subscription the Subscription object to display on this component
     */
    public SubscriptionView(Context context, Subscription subscription) {
        super(context);
        this.subscription = subscription;
        initSubView(context);
    }

    /**
     * Initialize the various values of this component.
     * @param context the current application context
     */
    private void initSubView(Context context) {
        LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.component_subscriptionview, this);

        textName = findViewById(R.id.subview_name);
        textCost = findViewById(R.id.subview_cost);
        textStartDate = findViewById(R.id.subview_startdate);

        textName.setText(subscription.getName());
        textCost.setText("Cost: $" + subscription.getCost());
        textStartDate.setText(new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(subscription.getStartDate()));
    }
}
