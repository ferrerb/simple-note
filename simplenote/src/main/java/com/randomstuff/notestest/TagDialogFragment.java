package com.randomstuff.notestest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class TagDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    //TODO everything. Do all the query in an asyncqueryhandler. 
    private TagDialogCallbacks mCallbacks;
    private View form = null;
    private static final String TAG_ID = "id";

    public TagDialogFragment newInstance(long id) {
        TagDialogFragment frag = new TagDialogFragment();

        Bundle args = getArguments();
        args.putLong(TAG_ID, id);
        frag.setArguments(args);

        return frag;
    }

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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        form = getActivity().getLayoutInflater().inflate(R.layout.tag_dialog_frag, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return (builder.setTitle(R.string.tag_dialog_title).setView(form)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, null)).create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        // do things! like TagDialogCallbacks
    }

    @Override
    public void onDetach() {
        mCallbacks = null;
        super.onDetach();
    }

}
