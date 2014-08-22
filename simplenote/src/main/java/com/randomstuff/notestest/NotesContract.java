package com.randomstuff.notestest;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * A contract class for the Notestest provider. Defines the various ways
 * to interact with the provider, such as Uris and table information.
 */
public final class NotesContract {
    /**
     *  Don't allow this class to be instantiated
     */
    private NotesContract() {}

    /**
     *  Authority definitions for the notes provider
     */
    public static final String AUTHORITY = "com.randomstuff.notestest.Provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

    /**
     *  Class to hold definitions for the Notes table
     */
    public static final class Notes implements BaseColumns {
        /**
         * Don't allow this class to be instantiated
         */
        private Notes() {}

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, "notes");
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_NOTE_MODIFIED = "note_mod";
        public static final String SORT_ORDER_DEFAULT = COLUMN_NOTE_MODIFIED + " DESC";
    }

    /**
     * Class to hold definitions for the virtual notes table. This is an FTS table used
     * for faster text searches.
     */
    public static final class NotesVirtual implements BaseColumns {
        /**
         * Don't allow this class to be instantiated
         */
        private NotesVirtual() {}

        public static final Uri CONTENT_URI =
                Uri.withAppendedPath(AUTHORITY_URI, "notes_virtual");
        public static final String TABLE_NAME = "notes_virtual";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NOTE = "note";
    }

}
