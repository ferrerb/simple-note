package com.randomstuff.notestest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * This class is called only on portrait mode. It either
 * calls the note fragment for a new note, or to display
 * an existing one.
 **/

public class NoteActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notes_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        if (savedInstanceState == null) {
            NoteFragment frag = new NoteFragment();
            frag.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.note, frag).commit();
        }
    }
}
