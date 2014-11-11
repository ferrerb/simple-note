package com.randomstuff.notestest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class TagDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private TagDialogCallbacks mCallbacks;
    private View mForm = null;
    private static final String TAG_ID = "id";
    private long mCurTagId = 0L;
    private int mSelectedPosition = -1;
    private long mSelectedTag = -1L;
    private Cursor c;
    private ListView lv;
    private SimpleCursorAdapter mAdapter;
    private static final int LOADER_ID = 2;

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

        mCurTagId = getArguments().getLong(TAG_ID, 0L);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        mForm = getActivity().getLayoutInflater().inflate(R.layout.tag_dialog_frag, null);
        builder.setView(mForm);

        lv = (ListView) mForm.findViewById(R.id.tag_dialog_list);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                new String[]{ NotesContract.Tags.COLUMN_TAGS},
                new int[]{android.R.id.text1},
                0);
        lv.setAdapter(mAdapter);
        getLoaderManager().initLoader(LOADER_ID, null, this);

        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedPosition = position;
                mSelectedTag = id;
                lv.setItemChecked(mSelectedPosition, true);
            }
        });

        return (builder.setTitle(R.string.tag_dialog_title)
                .setPositiveButton(R.string.ok, this)
                .setNeutralButton(R.string.remove_tag, this)
                .setNegativeButton(R.string.cancel, null)).create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        // do things! like TagDialogCallbacks
        // this stuff should happen in the positive button click -1 positive -2 negative
        if (which == -1) {
            EditText editNewTag = (EditText) mForm.findViewById(R.id.edit_new_tag);
            String newTag = editNewTag.getText().toString();

            if (newTag.length() > 0L) {
                mCallbacks.onTagChosen(newTag, -1L);
            }
            if (newTag.length() < 1L && mSelectedTag != mCurTagId) {
                if (c != null) {
                    c.moveToPosition(mSelectedPosition);
                    newTag = c.getString(c.getColumnIndex(NotesContract.Tags.COLUMN_TAGS));
                }
                Log.d("chosen tag in TagDialogFragment = ", newTag);
                mCallbacks.onTagChosen(newTag, mSelectedTag);
            }
        }
        if (which == -3) {
            // this is neutral button, remove tag
            Log.d("negative 2 !", "!");
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        mCallbacks = null;
        c.close();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        // set up the provider and uri etc
        return new CursorLoader(getActivity(),
                NotesContract.Tags.CONTENT_URI, null, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        c = data;
        mAdapter.swapCursor(data);

        // If the note already has a tag, this will mark that tag on the listview
        if (mCurTagId > 0L && c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                if (c.getLong(0) == mCurTagId) {
                    mSelectedPosition = c.getPosition();
                }
                c.moveToNext();
            }
            lv.setItemChecked(mSelectedPosition, true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
