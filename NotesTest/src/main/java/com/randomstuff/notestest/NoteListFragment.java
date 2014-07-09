package com.randomstuff.notestest;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class NoteListFragment extends ListFragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private boolean mDualPane;                          // To store the current layout
    private long mCurNotePosition;                      // Stores the current note _id
    private int index;
    private SimpleCursorAdapter adapter = null;         // Populates the listview

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
        outState.putLong("curNote", mCurNotePosition);          // Saving the current note _id
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.options, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar / menu item clicks here
        switch (item.getItemId()) {
            case(R.id.add_note):
                //call notefragment and make new note
                if (getFragmentManager().findFragmentById(R.id.notes) == null){
                    // this happens only in portrait mode
                    Intent i=new Intent(getActivity(), NoteActivity.class);
                    i.putExtra("id", 0L);
                    startActivity(i);
                }
                else {
                    NoteFragment noteFrag;
                    noteFrag = NoteFragment.newInstance(0L);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.notes, noteFrag).commit();
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
            getListView().setSelection(index);
            getListView().setSelected(true);

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
        adapter = new SimpleCursorAdapter(getActivity(),                // Context
                android.R.layout.simple_list_item_1,                    // provides a layout
                null,                                                   // empty initial cursor
                new String[] { Provider.Constants.COLUMN_TITLE },       // table column to get strings from
                new int[] {android.R.id.text1},
                0);

        setListAdapter(adapter);                                        // Sets current listview to the cursoradapter

        getLoaderManager().initLoader(0, null, this);                   // Begins cursorloader
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        String[] projection =
                { Provider.Constants.COLUMN_ID, Provider.Constants.COLUMN_TITLE };
        String sortOrder = Provider.Constants.COLUMN_ID + " DESC";

        return new CursorLoader(getActivity(), Provider.Constants.CONTENT_URI,
                                projection, null, null, sortOrder);
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
