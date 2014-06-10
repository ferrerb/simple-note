package com.randomstuff.notestest;

import android.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;

public class NoteFragment extends Fragment implements DatabaseHelper.NoteListener {
    private EditText editTitle=null;
    private EditText editNote=null;

    static NoteFragment newInstance(int position){
        NoteFragment frag = new NoteFragment();

        Bundle args = new Bundle();
        args.putInt("position", position);
        frag.setArguments(args);

        return(frag);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View result = inflater.inflate(R.layout.notes_detail, container, false);

        editTitle = (EditText)result.findViewById(R.id.edit_title);
        editNote = (EditText)result.findViewById(R.id.edit_note);

        return(result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notes, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void setNote(String[] note) {
        editTitle.setText(note[0]);
        editNote.setText(note[1]);
    }

    @Override
    public void onPause(){
        //use contentvalues to store from edittext, put in database
    }
}
