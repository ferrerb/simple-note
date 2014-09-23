package com.randomstuff.notestest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TagDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    //TODO everything. Do all the query in an asyncqueryhandler. 
    private TagDialogCallbacks mCallbacks;
    private View form = null;
    private static final String TAG_ID = "id";
    Cursor c;
    private ListView lv;

    public static TagDialogFragment newInstance(long id) {
        TagDialogFragment frag = new TagDialogFragment();

        Bundle args = new Bundle();
        args.putLong(TAG_ID, id);
        frag.setArguments(args);

        return frag;
    }

    public interface TagDialogCallbacks {
        void onTagChosen(String tag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            mCallbacks = (TagDialogCallbacks) getTargetFragment();
        } catch (ClassCastException e) {
            throw new ClassCastException(getTargetFragment().toString() +
                    " must implement TagDialogCallbacks");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        form = getActivity().getLayoutInflater().inflate(R.layout.tag_dialog_frag, null);
        lv = (ListView)form.findViewById(R.id.tag_dialog_list);
        c = getActivity().getContentResolver().query(
                NotesContract.Tags.CONTENT_URI,
                new String[]{ NotesContract.Tags.COLUMN_ID, NotesContract.Tags.COLUMN_TAGS },
                null,
                null,
                null);
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                c,
                new String[]{ NotesContract.Tags.COLUMN_TAGS},
                new int[]{android.R.id.text1},
                0);
        lv.setAdapter(adapter);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        return (builder.setView(form).setTitle(R.string.tag_dialog_title)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, null)).create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        // do things! like TagDialogCallbacks
        EditText editNewTag = (EditText) form.findViewById(R.id.edit_new_tag);
        String newTag = editNewTag.getText().toString();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mCallbacks = null;
        c.close();
        super.onDismiss(dialog);
    }

}
