package com.randomstuff.notestest;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;

public class SimpleDisplayActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            // take this out?
            String file = getIntent().getStringExtra("file");
            SimpleDisplayFragment f = new SimpleDisplayFragment();
            f.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
        }
    }
}
