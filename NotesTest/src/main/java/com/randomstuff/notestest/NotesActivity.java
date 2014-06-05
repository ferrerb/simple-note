package com.randomstuff.notestest;

import android.app.Activity;
import android.os.Bundle;

public class NotesActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        if (getFragmentManager().findFragmentById(R.id.notes) == null) {
            
        }
    }
}
