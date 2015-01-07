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
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

/** Used to choose, create and remove a tag from a note */
public class TagDialogFragment extends DialogFragment
        implements DialogInterface.OnClickListener, LoaderManager.LoaderCallbacks<Cursor> {
    private TagDialogCallbacks mCallbacks;
    private View mForm = null;
    private static final String TAG_ID = "id";
    private long mCurTagId = 0L;
    private int mSelectedPosition = -1;
    private long mSelectedTag = -1L;
    private Cursor c;
    private AutoCompleteTextView mAutoView;
    private SimpleCursorAdapter mAdapter;
    private static final int LOADER_ID = 2;

    /** Returns a new fragment with the id of the note's current tag in a bundle
     *
     * @param id long
     * @return TagDialogFragment
     */
    public static TagDialogFragment newInstance(long id) {
        TagDialogFragment frag = new TagDialogFragment();

        Bundle args = new Bundle();
        args.putLong(TAG_ID, id);
        frag.setArguments(args);

        return frag;
    }

    /** Provides the selected tags name and id */
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

        mAutoView = (AutoCompleteTextView) mForm.findViewById(R.id.tag_auto_text_view);

        mAdapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1,
                null,
                new String[]{ NotesContract.Tags.COLUMN_TAGS},
                new int[]{android.R.id.text1},
                0);
        mAutoView.setAdapter(mAdapter);
        mAutoView.setThreshold(2);
        mAutoView.showDropDown();
        // TODO move from cursorloader since we arent using a listview
        getLoaderManager().initLoader(LOADER_ID, null, this);

        mAutoView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedPosition = position;
                mSelectedTag = id;
            }
        });

        return (builder.setTitle(R.string.tag_dialog_title)
                .setPositiveButton(R.string.ok, this)
                .setNeutralButton(R.string.remove_tag, this)
                .setNegativeButton(R.string.cancel, null)).create();
    }


    @Override
    public void onClick(DialogInterface dialog, int which) {
        // Positive button, so this means adding or changing a tag
        if (which == DialogInterface.BUTTON_POSITIVE) {
            String newTag = mAutoView.getText().toString();
            // A new tag entered in the textview, although this might have some logic issues
            if (newTag.length() > 0L && mSelectedPosition < 0) {
                mCallbacks.onTagChosen(newTag, -1L);
            }
            // A new tag chosen from the list of existing tags
            if (newTag.length() > 0L && mSelectedTag != mCurTagId && mSelectedPosition > -1) {
                Log.d("chosen tag in TagDialogFragment = ", newTag);
                mCallbacks.onTagChosen(newTag, mSelectedTag);
            }
        }
        // neutral button, which is used here to remove the current tag
        if (which == DialogInterface.BUTTON_NEUTRAL) {
            mCallbacks.onTagChosen(null, -2L);
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
        mAutoView.showDropDown();
        // If the note already has a tag, this will mark that tag on the listview
        if (mCurTagId > 0L && c != null && c.moveToFirst()) {
            while (!c.isAfterLast()) {
                if (c.getLong(0) == mCurTagId) {
                    mSelectedPosition = c.getPosition();
                }
                c.moveToNext();
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

}
