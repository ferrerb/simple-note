package com.randomstuff.notestest;

import android.content.AsyncQueryHandler;
import android.content.ContentResolver;

public class NoteAsyncQueryHandler extends AsyncQueryHandler {
    public NoteAsyncQueryHandler (ContentResolver cr) {
        super(cr);
    }
}
