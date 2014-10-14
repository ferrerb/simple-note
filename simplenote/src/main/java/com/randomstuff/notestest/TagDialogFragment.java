package com.randomstuff.notestest;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TagDialogFragment extends DialogFragment implements DialogInterface.OnClickListener {
    private TagDialogCallbacks mCallbacks;
    private View form = null;
    private static final String TAG_ID = "id";
    private long curTagId = 0L;
    private int selectedPosition = -1;
    private long selectedTag = -1L;
    private Cursor c;
    private ListView lv;

    public static TagDialogFragment newInstance(long id) {
        TagDialogFragment frag = new TagDialogFragment();

        Bundle args = new Bundle();
        args.putLong(TAG_ID, id);
        frag.setArguments(args);

        return frag;
    }

    public interface TagDialogCallbacks {
        void onTagChosen(String tag, long id);
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

        curTagId = getArguments().getLong(TAG_ID, 0L);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        form = getActivity().getLayoutInflater().inflate(R.layout.tag_dialog_frag, null);
        builder.setView(form);

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
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedPosition = position;
                selectedTag = id;
                lv.setItemChecked(selectedPosition, true);
            }
        });

        if (curTagId > 0L) {
            if (c.moveToFirst()) {
                while (!c.isAfterLast()) {
                    if (c.getLong(0) == curTagId) {
                        selectedPosition = c.getPosition();
                    }
                }
            }
            lv.performItemClick(lv, selectedPosition, curTagId);
        }

        return (builder.setTitle(R.string.tag_dialog_title)
                .setPositiveButton(R.string.ok, this)
                .setNegativeButton(R.string.cancel, null)).create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        // do things! like TagDialogCallbacks
        // this stuff should happen in the positive button click -1 positive -2 negative
        if (which == -1) {
            EditText editNewTag = (EditText) form.findViewById(R.id.edit_new_tag);
            String newTag = editNewTag.getText().toString();

            if (newTag.length() > 0L) {
                mCallbacks.onTagChosen(newTag, -1L);
            }
            if (newTag.length() < 1L && selectedTag != curTagId) {
                newTag = lv.getItemAtPosition(selectedPosition).toString();
                Log.d("chosen tag in TagDialogFragment = ", newTag);
                mCallbacks.onTagChosen(newTag, selectedTag);
            }
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mCallbacks = null;
    }

}
