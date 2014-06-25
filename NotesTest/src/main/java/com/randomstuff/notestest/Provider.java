package com.randomstuff.notestest;

import android.content.ContentProvider;

public class Provider extends ContentProvider {
    private DatabaseHelper db;

    public boolean onCreate() {
        db= new DatabaseHelper(getContext());
        return ((db != null));
    }

    public void query() {

    }

}

