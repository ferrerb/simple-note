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

        //need to change this to check for both fragments or something
        View notesListFrame = findViewById(R.id.notes_list);
        boolean mDualPane = (notesListFrame != null) && (notesListFrame.getVisibility() == View.VISIBLE);

        Intent intent = getIntent();
        String action = intent.getAction();
        String shareType = intent.getType();
    }
}
