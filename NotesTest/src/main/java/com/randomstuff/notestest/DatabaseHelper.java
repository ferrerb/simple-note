package com.randomstuff.notestest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notestest.db";
    private static final int SCHEMA_VERSION = 2;

    // The names of columns and the table for the notes
    private static final String TABLE_NOTES ="notes";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_NOTE_MODIFIED = "note_mod";

    // Name for a FTS table to be implemented, to allow text searches
    private static final String VIRTUAL_TABLE_NOTES = "notes_virtual";

    public DatabaseHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creates the database with beautiful sql syntax
        /* TODO add 2 tables, one to hold notebook/tag types, and 1 to hold note=tag */
        try {
            db.beginTransaction();
            db.execSQL("CREATE TABLE " + TABLE_NOTES + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_NOTE + " TEXT, " +
                    COLUMN_NOTE_MODIFIED + " INTEGER);");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        /* Adds tables or whatever is needed based on the version of the database */
        switch (oldVersion) {
            case 1:
                db.execSQL("ALTER TABLE " +
                        TABLE_NOTES + " ADD COLUMN " +
                        COLUMN_NOTE_MODIFIED + " INTEGER");
        }
    }
}