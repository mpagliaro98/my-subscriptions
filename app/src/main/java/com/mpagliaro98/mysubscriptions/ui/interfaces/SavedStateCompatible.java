package com.mpagliaro98.mysubscriptions.ui.interfaces;

import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * An interface to denote pages that are compatible with saved state bundles. A saved
 * state compatible page will have functionality to fill a given bundle with the saved
 * state it has, then apply that state from the bundle back to its view.
 */
public interface SavedStateCompatible {

    /**
     * Populate a given bundle with values pertaining to how this fragment is set. These
     * values should be indexed by static keys defined in the classes where this is
     * implemented.
     * @param bundle the bundle to place the saved items in
     */
    void fillBundleWithSavedState(Bundle bundle);

    /**
     * Given a bundle of saved state, extract the values that were saved to it previously
     * and re-apply them to this view. This is best done with a series of if statements,
     * checking that each needed key is in the bundle before applying it, as the bundle will
     * likely have more than just the given view's saved state in it.
     * @param savedState bundle of saved state, must not be null
     * @param root the root view of this tab
     */
    void applySavedState(@NonNull final Bundle savedState, View root);
}
