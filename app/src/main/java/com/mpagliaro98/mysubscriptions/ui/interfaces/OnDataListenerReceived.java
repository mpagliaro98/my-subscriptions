package com.mpagliaro98.mysubscriptions.ui.interfaces;

import com.mpagliaro98.mysubscriptions.model.Subscription;
import com.mpagliaro98.mysubscriptions.ui.MainActivity;

/**
 * Fragments that implement this interface will be a receiver of new Subscription
 * objects. onDataReceived will be called in the data listener each time a new Subscription
 * object arrives, and the receiver will handle what to do with the incoming data. This
 * is designed to be implemented by one of the tabs under MainActivity.
 */
public interface OnDataListenerReceived {

    /**
     * Receive data from another activity, passed to here through this fragment's
     * parent activity. In this case, the data is a subscription object modified in
     * a separate activity, which we will perform an operation on depending on what
     * action should be taken.
     * @param subscription the new subscription object
     * @param type the action to take on the incoming data, either CREATE, EDIT, or DELETE
     * @param subIndex if required, the index in the list of the item to modify
     */
    void onDataReceived(Subscription subscription, MainActivity.INCOMING_TYPE type, Integer subIndex);
}
