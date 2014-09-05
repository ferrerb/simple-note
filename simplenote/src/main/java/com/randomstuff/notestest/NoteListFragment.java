package com.randomstuff.notestest;

import android.app.FragmentTransaction;
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
    // To store the current layout
    private boolean mDualPane;
    // Stores the current note _id
    private long mCurNotePosition;
    private int index;
    // Populates the listview
    private SeparatorCursorAdapter adapter = null;
    private String mCurrentFilter = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.notes_list, container, false);

        setHasOptionsMenu(true);

        return(result);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // A function that starts the cursorloader
        fillList();

        // Basic check for the layout, borrowed from google
        View notesFrame = getActivity().findViewById(R.id.notes);
        mDualPane = (notesFrame != null) && (notesFrame.getVisibility() == View.VISIBLE);

        // checks the saved bundle for a note to display based on _id
        if (savedInstanceState != null) {
            mCurNotePosition = savedInstanceState.getLong("curNote", 1L);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        // Saving the current note _id
        outState.putLong("curNote", mCurNotePosition);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.options, menu);
        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setOnQueryTextListener(this);

    }

    public boolean onQueryTextChange(String newText) {
        mCurrentFilter = !TextUtils.isEmpty(newText) ? newText : null;
        getLoaderManager().restartLoader(0, null, this);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar / menu item clicks here
        switch (item.getItemId()) {
            case(R.id.add_note):
                //call notefragment and make new note

                if (mDualPane) {
                    NoteFragment noteFrag = (NoteFragment)getFragmentManager().findFragmentById(R.id.notes);
                    if (noteFrag == null || noteFrag.getShownId() > -2L) {
                        noteFrag = NoteFragment.newInstance(0L);
                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.notes, noteFrag).commit();
                    }
                }
                else {
                    // this happens only in portrait mode
                    Intent i=new Intent(getActivity(), NoteActivity.class);
                    i.putExtra("id", 0L);
                    startActivity(i);
                }
                return true;
            case(R.id.settings):
                return true;
            case(R.id.help):
                Intent i = new Intent(getActivity(), SimpleDisplayActivity.class);
                i.putExtra("file", "help.txt");
                startActivity(i);
                return true;
            case(R.id.about):
                i = new Intent(getActivity(), SimpleDisplayActivity.class);
                i.putExtra("file", "about.txt");
                startActivity(i);
                return true;
        }
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

            NoteFragment noteFrag = (NoteFragment)
                    getFragmentManager().findFragmentById(R.id.notes);

            if (noteFrag == null || noteFrag.getShownId() != id) {
                noteFrag = NoteFragment.newInstance(id);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.notes, noteFrag).commit();
            }
        }
        else {
            Intent i = new Intent();
            i.setClass(getActivity(), NoteActivity.class);
            i.putExtra("id", id);
            startActivity(i);
        }
    }

    private void fillList() {
        adapter = new SeparatorCursorAdapter(getActivity(), null, 0);
        // Sets current listview to the cursoradapter
        setListAdapter(adapter);
        // Begins cursorloader
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Uri baseUri;
        String[] projection;
        String[] selectionArgs;
        if (mCurrentFilter != null && mCurrentFilter.length() > 2) {
            // add the filter to a filter URI
            // the filter URI should use the virtual table
            Log.d("currentfilter = ", mCurrentFilter);
            baseUri = NotesContract.NotesVirtual.CONTENT_URI;
            projection = new String[] {
                    NotesContract.Notes.COLUMN_ID,
                    NotesContract.Notes.COLUMN_TITLE,
                    NotesContract.Notes.COLUMN_NOTE,
                    NotesContract.Notes.COLUMN_NOTE_MODIFIED };
            selectionArgs = new String[] { mCurrentFilter + "*" };
        } else {
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
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
