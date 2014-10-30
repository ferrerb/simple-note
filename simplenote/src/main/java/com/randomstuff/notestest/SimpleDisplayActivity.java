package com.randomstuff.notestest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

public class SimpleDisplayActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getFragmentManager().findFragmentById(android.R.id.content) == null) {
            SimpleDisplayFragment f = new SimpleDisplayFragment();
            f.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(android.R.id.content, f).commit();
        }
    }
}
