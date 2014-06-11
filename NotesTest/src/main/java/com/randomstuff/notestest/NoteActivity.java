package com.randomstuff.notestest;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

/**
 * This class is called only on portrait mode. It either
 * calls the note fragment for a new note, or to display
 * an existing one.
 **/

public class NoteActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState == null) {
            NoteFragment frag = new NoteFragment();
            frag.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(android.R.id.content, frag).commit();
        }
    }
}
