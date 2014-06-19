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

    static NoteFragment newInstance(int index){
        NoteFragment frag = new NoteFragment();

        Bundle args = new Bundle();
        args.putInt("index", index);
        frag.setArguments(args);

        return(frag);
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
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
        if (getShownIndex() == -1) {
            /*
            *Call createnoteasync or whatever, just display blank stuff, no need for db work
            *could just set blank here, and call create note or something at onPause, based on
            * index -1
            */
            editTitle=null;
            editNote=null;
        }
        else{
            DatabaseHelper.getInstance(getActivity()).getNoteAsync(getShownIndex(), this);
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

        }
        return(true);
    }

    @Override
    public void setNote(String[] note) {
        editTitle.setText(note[0]);
        editNote.setText(note[1]);
    }

    @Override
    public void onPause(){
        super.onPause();
        DatabaseHelper.getInstance(getActivity()).saveNoteAsync(getShownIndex(),
                                   editTitle.getText().toString(),
                                   editNote.getText().toString());
    }
}
