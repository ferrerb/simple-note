package com.randomstuff.notestest;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class NotesTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the initial layout, currently based on either landscape or portrait
        setContentView(R.layout.main);

        Intent intent = getIntent();
        String action = intent.getAction();
        String shareType = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(shareType)) {
            startShare(intent);
        }

    }

    private void startShare(Intent intent) {
        //need to change this to check for both fragments or something
        // possibly do this in its own activity
        // use the code from notelistfragment for creating new note
        //
        View notesListFrame = findViewById(R.id.notes_list);
        View notesFrame = findViewById(R.id.notes);
        boolean mDualPane = (notesListFrame != null) && (notesListFrame.getVisibility() == View.VISIBLE);

        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // do something, ie use code from notelistfragment new note
            //check for dual pane, use noteactivity or not based on that
        }
    }
}
