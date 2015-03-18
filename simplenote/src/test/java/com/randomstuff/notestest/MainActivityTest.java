package com.randomstuff.notestest;

import android.test.ActivityInstrumentationTestCase2;

import com.randomstuff.notestest.MainActivity;


public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {
    public MainActivityTest() {
        super(MainActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        setActivityInitialTouchMode(false);
        MainActivity activity=getActivity();
    }


}
