package com.randomstuff.notestest;

import android.app.Activity;
import android.content.res.Configuration;
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

        // Exits this activity if the orientation changes to landscape
        // TODO Should get rid of this when decide to make layout based on screen size
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            finish();
            return;
        }

        if (savedInstanceState == null) {
            NoteFragment frag = new NoteFragment();
            frag.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(android.R.id.content, frag).commit();
        }
    }
}
