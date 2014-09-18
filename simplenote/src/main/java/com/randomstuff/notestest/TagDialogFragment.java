package com.randomstuff.notestest;

import android.app.Activity;
import android.app.DialogFragment;

public class TagDialogFragment extends DialogFragment {
    private TagDialogCallbacks mCallbacks;

    public interface TagDialogCallbacks {
        void onTagChosen(String tag);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallbacks = (TagDialogCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement TagDialogCallbacks");
        }
    }

}
