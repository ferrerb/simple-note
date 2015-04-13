package com.randomstuff.notestest.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.randomstuff.notestest.R;
import com.randomstuff.notestest.adapters.SeparatorCursorAdapter;
import com.randomstuff.notestest.data.NotesContract;

public class NoteListFragment extends ListFragment implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    // Part of a callback interface
    OnNoteSelectedListener mCallback;
    // To store the current layout
    private boolean mDualPane;
    // Stores the current note _id
    private long mCurNotePosition;
    private int mIndex;
    // Populates the listview
    private SeparatorCursorAdapter mAdapter = null;
    private String mCurrentFilter = null;
    // A static id to give the cursorloader, to allow for new queries based on tags/searches
    private static final int LOADER_ID = 0;
    private static final String CURRENT_NOTE_ID = "curNote";
    // Variables to hold the id for a tag passed in from the navigation drawer -> mainactivity
    private static final String TAG_ID = "id";
    private long mTagId = -1L;

    /** Returns a new fragment, with a bundle containing a tag id indicating which notes to list */
    public static NoteListFragment newInstance(long id) {
        NoteListFragment frag = new NoteListFragment();

        Bundle args = new Bundle();
        args.putLong(TAG_ID, id);
        frag.setArguments(args);

        return frag;
    }

    /** Provides the selected notes id to the parent activity */
    public interface OnNoteSelectedListener {
        public void onNoteSelected(long id);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mCallback = (OnNoteSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement OnNoteSelectedListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallback = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        if ( getArguments() != null) {
            mTagId = getArguments().getLong(TAG_ID, -1L);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.notes_list, container, false);

        // Method to request all the notes and fill the listview
        fillList();

        // Checks to see if the layout is dual pane
        View notesFrame = getActivity().findViewById(R.id.notes);
        mDualPane = (notesFrame != null) && (notesFrame.getVisibility() == View.VISIBLE);

        // checks the saved bundle for a note to display based on _id
        if (savedInstanceState != null) {
            mCurNotePosition = savedInstanceState.getLong(CURRENT_NOTE_ID, 1L);
        }
        // if the layout has both panes, shows that saved note
        if (mDualPane) {

            if (mCurNotePosition != 0) {
                showNote(mCurNotePosition);
            }
        }
        setHasOptionsMenu(true);

        return(result);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving the current note _id
        outState.putLong(CURRENT_NOTE_ID, mCurNotePosition);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        Log.d("Options menu called", "yes");
        inflater.inflate(R.menu.notes_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(this);

    }

    /** Monitors the search view, and restarts the cursorloader for the note list, show the
     * notes with the entered text. It begins searching after 3 characters have been entered.
     * @param newText string Text entered in the search view
     * @return boolean
     */
    public boolean onQueryTextChange(String newText) {
        mCurrentFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(LOADER_ID, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        return super.onOptionsItemSelected(item);
    }

    /** Provides information about the selected item in the listview. Since this is a list fragment
     *  it expects this method to be implemented.
     * @param l ListView
     * @param v View
     * @param position integer
     * @param id long
     */
    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        mIndex = position;
        showNote(id);
    }

    /** Sends the selected notes id to the parent activity, and sets the listview item selected, to
     *  provide visual confirmation of the choice
     * @param id long
     */
    private void showNote(long id) {
        mCurNotePosition = id;

        if (mDualPane) {
            getListView().setItemChecked(mIndex, true);
        }
        mCallback.onNoteSelected(id);
    }

    /** Creates a new adapter and beings a cursor loader to fill the listview */
    private void fillList() {
        mAdapter = new SeparatorCursorAdapter(getActivity(), null, 0);
        // Sets current listview to the cursoradapter
        setListAdapter(mAdapter);
        // Begins cursorloader
        getLoaderManager().initLoader(LOADER_ID, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Uri baseUri;
        String[] projection;
        String[] selectionArgs;
        if (mCurrentFilter != null && mCurrentFilter.length() > 2) {
            // add the filter to a filter URI
            // the filter URI should use the virtual table
            baseUri = NotesContract.NotesVirtual.CONTENT_URI;
            projection = new String[] {
                    NotesContract.Notes.COLUMN_ID,
                    NotesContract.Notes.COLUMN_TITLE,
                    NotesContract.Notes.COLUMN_NOTE,
                    NotesContract.Notes.COLUMN_NOTE_MODIFIED };
            selectionArgs = new String[] { mCurrentFilter + "*" };
        } else if (mTagId > -1L) {
            // stuff to show only that tag or, do i need this. To have an 'All notes' choice, i need
            // to either have the first entry in tags table be for all, or have separate button for all
            baseUri = NotesContract.Tags.TAGS_NOTES;
            projection = new String[] {
                    NotesContract.Notes.COLUMN_ID,
                    NotesContract.Notes.COLUMN_TITLE,
                    NotesContract.Notes.COLUMN_NOTE,
                    NotesContract.Notes.COLUMN_NOTE_MODIFIED };
            selectionArgs = new String[] { Long.toString(mTagId) };
            Log.d("tagid from notelistfragment = ", selectionArgs[0]);
        }
        else {
            baseUri = NotesContract.Notes.CONTENT_URI;
            projection = new String[] {
                    NotesContract.Notes.COLUMN_ID,
                    NotesContract.Notes.COLUMN_TITLE,
                    NotesContract.Notes.COLUMN_NOTE,
                    NotesContract.Notes.COLUMN_NOTE_MODIFIED };
            selectionArgs = null;
        }

        return new CursorLoader(getActivity(), baseUri,
                                projection, null, selectionArgs,
                                NotesContract.Notes.SORT_ORDER_DEFAULT);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
