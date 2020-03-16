package com.mpagliaro98.mysubscriptions.ui.components;

import android.content.Context;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.widget.ImageView;
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
        TextView textName = findViewById(R.id.subview_name);
        TextView textCost = findViewById(R.id.subview_cost);
        TextView textNextDate = findViewById(R.id.subview_startdate);
        ImageView imageColor = findViewById(R.id.subview_color);

        // Set the text in each part of the component
        textName.setText(subscription.getName());
        String costStr = "$" + String.format("%.2f", subscription.getCost()) + " " +
                getRechargeFrequencyString(subscription.getRechargeFrequency(), context);
        textCost.setText(costStr);
        String nextDateStr = "Next Payment Date: " +
                new SimpleDateFormat("MM/dd/yyyy", Locale.US).format(subscription.getNextPaymentDate());
        textNextDate.setText(nextDateStr);
        imageColor.setColorFilter(context.getResources().getColor(subscription.getCategory().getColor()),
                PorterDuff.Mode.SRC_IN);
    }

    /**
     * Convert a recharge frequency integer value into its corresponding string.
     * @param rechargeFrequency the recharge frequency in months as an integer
     * @param context the current application context
     * @return the string that represents the valid time frame
     */
    private String getRechargeFrequencyString(int rechargeFrequency, Context context) {
        if (rechargeFrequency == 1) {
            return context.getString(R.string.array_freq_monthly);
        } else if (rechargeFrequency == 2) {
            return context.getString(R.string.array_freq_bimonthly);
        } else if (rechargeFrequency == 3) {
            return context.getString(R.string.array_freq_trimonthly);
        } else if (rechargeFrequency == 6) {
            return context.getString(R.string.array_freq_twiceyear);
        } else if (rechargeFrequency == 12) {
            return context.getString(R.string.array_freq_yearly);
        } else {
            return "";
        }
    }
}
