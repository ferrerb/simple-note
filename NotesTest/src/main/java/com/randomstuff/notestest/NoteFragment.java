package com.randomstuff.notestest;

import android.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

public class NoteFragment extends Fragment implements DatabaseHelper.NoteListener {
    private EditText editTitle=null;
    private EditText editNote=null;

    static NoteFragment newInstance(long id, int index){
        NoteFragment frag = new NoteFragment();

        Bundle args = new Bundle();
        args.putInt("index", index);
        args.putLong("id", id);
        frag.setArguments(args);

        return(frag);
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    public long getShownId() {
        return getArguments().getLong("id", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View result = inflater.inflate(R.layout.notes_detail, container, false);

        editTitle = (EditText)result.findViewById(R.id.edit_title);
        editNote = (EditText)result.findViewById(R.id.edit_note);

        //if logic about -1 making a new note, otherwise getnoteasync
        if (getShownIndex() != -1) {
            DatabaseHelper.getInstance(getActivity()).getNoteAsync(getShownId(), this);
        }

        return(result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notes, menu);

    }

    @Override
    public boolean onOptionsItemSelected (MenuItem item) {
        //deal with delete
        if (item.getItemId() == R.id.delete) {
            //call delete note and maybe move to another note
            //must do something diff for portrait and land
            DatabaseHelper.getInstance(getActivity()).deleteNoteAsync(getShownId());

        }
        //implement a 'Done' button or something
        return(true);
    }

    @Override
    public void setNote(String[] note) {
        editTitle.setText(note[0]);
        editNote.setText(note[1]);
    }

    @Override
    public void onPause(){
        //Log.d("title check", "value : " + editTitle.getText().toString());
        DatabaseHelper.getInstance(getActivity()).saveNoteAsync(getShownIndex(),
                                   editTitle.getText().toString(),
                                   editNote.getText().toString());

        super.onPause();
    }
}
