package com.randomstuff.notestest;

import android.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;

public class NoteFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){

        View result = inflater.inflate(R.layout.notes_detail, container, false);
        return(result);
    }

    @Override
    public void onPause(){

    }
}
