package com.randomstuff.notestest;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class NoteListFragment extends ListFragment implements DatabaseHelper.ListListener {
    boolean mDualPane;
    long mCurNotePosition = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.notes_list, container, false);

        return(result);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // Uses the DatabaseHelper to populate the listview with the note titles
        DatabaseHelper.getInstance(getActivity()).getListAsync(this);

        View notesFrame = getActivity().findViewById(R.id.notes);
        mDualPane = (notesFrame != null) && (notesFrame.getVisibility() == View.VISIBLE);

        if (savedInstanceState != null) {
            mCurNotePosition = savedInstanceState.getLong("curNote", 1);
        }

        if (mDualPane) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            //might need to iterate through cursor and match the id to the position
            showNote(mCurNotePosition, 0);
        }
    }

    @Override
    public void setList(Cursor listHere) {
        //something with listNote
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(getActivity(),
                android.R.layout.simple_list_item_1, listHere, new String[] { "title" },
                new int[] { android.R.id.text1}, 0);
        setListAdapter(adapter);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong("curNote", mCurNotePosition);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);

        showNote(id, position);
    }

    void showNote(long id, int index) {
        // deal with showing the fragment
        mCurNotePosition = id;

        if (mDualPane) {
            // Highlights the currently selected note
            getListView().setItemChecked(index, true);

            NoteFragment noteFrag = (NoteFragment)
                    getFragmentManager().findFragmentById(R.id.notes);

            if (noteFrag == null || noteFrag.getShownIndex() != index) {
                noteFrag = NoteFragment.newInstance(id, index);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.notes, noteFrag).commit();
            }
        }
        else {
            Intent i = new Intent();
            i.setClass(getActivity(), NoteActivity.class);
            i.putExtra("index", index);
            i.putExtra("id", id);
            startActivity(i);
        }
    }
}
