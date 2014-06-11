package com.randomstuff.notestest;

import android.app.FragmentTransaction;
import android.app.ListFragment;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class NoteListFragment extends ListFragment implements DatabaseHelper.ListListener {
    boolean mDualPane;
    int mCurNotePosition = 0;

    @Override
    public void onActivityCreated(Bundle savedInstanceState){
        super.onActivityCreated(savedInstanceState);

        // Uses the DatabaseHelper to populate the listview with the note titles
        DatabaseHelper.getInstance(getActivity()).getListAsync(this);

        View notesFrame = getActivity().findViewById(R.id.notes);
        mDualPane = notesFrame != null && notesFrame.getVisibility() == View.VISIBLE;

        if (savedInstanceState != null) {
            mCurNotePosition = savedInstanceState.getInt("curNote", 0);
        }

        if (mDualPane) {
            getListView().setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            showNote(mCurNotePosition);
        }
    }

    @Override
    public void setList(Cursor listHere) {
        //something with listNote
        SimpleCursorAdapter adapter = new SimpleCursorAdapter(this,
                android.R.layout.simple_list_item_1, listHere, from, to, flags);
        setListAdapter(adapter);
    }

    @Override
    public void onSavedInstanceState(Bundle outState) {
        super.onSavedInstanceState(outState);
        outState.putInt("curNote", mCurNotePosition);
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        showNote(position);
    }

    void showNote(int index) {
        // deal with showing the fragment
        mCurNotePosition = index;

        if (mDualPane) {
            // Highlights the currently selected note
            getListView().setItemChecked(index, true);

            NoteFragment noteFrag = (NoteFragment)
                    getFragmentManager().findFragmentById(R.id.notes);

            if (noteFrag == null || noteFrag.getShownIndex() != index) {
                noteFrag = NoteFragment.newInstance(index);

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.notes, noteFrag).commit();
            }
        }
        else {
            Intent i = new Intent();
            i.setClass(getActivity(), NoteActivity.class);
            i.putExtra("index", index);
            startActivity(i);
        }
    }
}
