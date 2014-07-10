package com.randomstuff.notestest;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NotesContract {

    private NotesContract() {}

    public static final String AUTHORITY = "com.randomstuff.notestest.Provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);
    public static final String BASE_PATH = "notes";
    public static final String DATABASE_NAME = "notestest.db";

    public static final class Notes implements BaseColumns {
        private Notes() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "notes");
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_NOTE_MODIFIED = "note_mod";
    }



}
