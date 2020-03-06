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
    private TextView textNextDate;

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

        // Get the TextViews that make up this component
        textName = findViewById(R.id.subview_name);
        textCost = findViewById(R.id.subview_cost);
        textNextDate = findViewById(R.id.subview_startdate);

        // Set the text in each part of the component
        textName.setText(subscription.getName());
        String costStr = "$" + String.format("%.2f", subscription.getCost()) + " " +
                subscription.getRechargeFrequency();
        textCost.setText(costStr);
        String nextDateStr = "Next Payment Date: " +
                new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(subscription.getNextPaymentDate());
        textNextDate.setText(nextDateStr);
    }
}
