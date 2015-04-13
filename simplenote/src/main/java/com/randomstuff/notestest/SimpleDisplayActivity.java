package com.randomstuff.notestest;

import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

import com.randomstuff.notestest.ui.SimpleDisplayFragment;

/** Simple class to host a fragment with the help/about information */
public class SimpleDisplayActivity extends ActionBarActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.simple_display_activity);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getFragmentManager().findFragmentById(R.id.simple_display_frag) == null) {
            SimpleDisplayFragment f = new SimpleDisplayFragment();
            f.setArguments(getIntent().getExtras());
            getFragmentManager().beginTransaction().add(R.id.simple_display_frag, f).commit();
        }
    }
}
