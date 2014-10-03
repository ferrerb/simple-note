package com.randomstuff.notestest;

import android.app.Activity;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SearchView;

public class NoteListFragment extends ListFragment implements SearchView.OnQueryTextListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    // Part of a callback interface
    OnNoteSelectedListener mCallback;
    // To store the current layout
    private boolean mDualPane;
    // Stores the current note _id
    private long mCurNotePosition;
    private int index;
    // Populates the listview
    private SeparatorCursorAdapter adapter = null;
    private String mCurrentFilter = null;
    // A static id to give the cursorloader, to allow for new queries based on tags/searches
    private static final int LOADER_ID = 0;
    private static final String CURRENT_NOTE_ID = "curNote";
    // Variables to hold the id for a tag passed in from the navigation drawer -> mainactivity
    private static final String TAG_ID = "id";
    private long tagId = -1L;

    public static NoteListFragment newInstance(long id) {
        NoteListFragment frag = new NoteListFragment();

        Bundle args = new Bundle();
        args.putLong(TAG_ID, id);
        frag.setArguments(args);

        return frag;
    }

    // Interface that hosting activity must implement
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

        // A function that starts the cursorloader
        if ( getArguments() != null) {
            tagId = getArguments().getLong(TAG_ID, -1L);
        }
        fillList();

        // Basic check for the layout, borrowed from google
        View notesFrame = getActivity().findViewById(R.id.notes);
        mDualPane = (notesFrame != null) && (notesFrame.getVisibility() == View.VISIBLE);

        // checks the saved bundle for a note to display based on _id
        if (savedInstanceState != null) {
            mCurNotePosition = savedInstanceState.getLong(CURRENT_NOTE_ID, 1L);
        }
        Log.d("mCurNotePosition = ", Long.toString(mCurNotePosition));
        // if the layout has both panes, shows that saved note
        if (mDualPane) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);

            if (mCurNotePosition != 0) {
                showNote(mCurNotePosition);
            }
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.notes_list, container, false);

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
        inflater.inflate(R.menu.notes_list, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(this);

    }

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

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        index = position;
        showNote(id);
    }

    void showNote(long id) {
        mCurNotePosition = id;

        if (mDualPane) {
            getListView().setItemChecked(index, true);
        }
        mCallback.onNoteSelected(id);
    }

    private void fillList() {
        adapter = new SeparatorCursorAdapter(getActivity(), null, 0);
        // Sets current listview to the cursoradapter
        setListAdapter(adapter);
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
        } else if (tagId > -1L) {
            // stuff to show only that tag or, do i need this. To have an 'All notes' choice, i need
            // to either have the first entry in tags table be for all, or have separate button for all
            baseUri = NotesContract.NotesVirtual.CONTENT_URI;
            projection = new String[] {
                    NotesContract.Notes.COLUMN_ID,
                    NotesContract.Notes.COLUMN_TITLE,
                    NotesContract.Notes.COLUMN_NOTE,
                    NotesContract.Notes.COLUMN_NOTE_MODIFIED };
            selectionArgs = new String[] { Long.toString(tagId) };
        }
        else {
            baseUri = NotesContract.Tags.NOTES_WITH_TAG;
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
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
