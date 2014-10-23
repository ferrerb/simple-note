package com.randomstuff.notestest;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

/**
 * Created by mag on 10/23/14.
 */
public class TagCursorAdapter extends CursorAdapter {

    public TagCursorAdapter(Context ctxt, Cursor cursor, int flags) {
        super(ctxt, cursor, flags);
    }

    @Override
    public void bindView(View v, Context c, Cursor cursor) {
    }

    @Override
    public ViewGroup newView(Context c, Cursor cursor, ViewGroup group) {
        return null;
    }
}
