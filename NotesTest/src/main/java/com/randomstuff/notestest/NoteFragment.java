package com.randomstuff.notestest;

import android.app.Fragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;

public class NoteFragment extends Fragment {

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
        return(result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.notes, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onPause(){

    }
}
