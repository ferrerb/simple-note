package com.randomstuff.notestest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notestest.db";
    private static final int SCHEMA_VERSION = 4;

    // The names of columns and the table for the notes
    private static final String TABLE_NOTES ="notes";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_NOTE = "note";
    private static final String COLUMN_NOTE_MODIFIED = "note_mod";

    // Name for a FTS table to be implemented, to allow text searches
    private static final String VIRTUAL_TABLE_NOTES = "notes_virtual";

    // Names for database triggers, to keep virtual table updated for now
    private static final String TRIGGER_BEFORE_UPDATE = "notes_bu";
    private static final String TRIGGER_BEFORE_DELETE = "notes_bd";
    private static final String TRIGGER_AFTER_UPDATE = "notes_au";
    private static final String TRIGGER_AFTER_INSERT = "notes_ai";

    // Names for a table to hold tags
    private static final String TABLE_TAGS = "tags";
    private static final String COLUMN_TAGS = "col_tags";

    // Names for a table to hold tags = notes
    private static final String TABLE_TAGS_NOTES = "tags_notes";
    private static final String COLUMN_TAGS_ID = "tags_id";
    private static final String COLUMN_NOTES_ID = "notes_id";

    // SQL strings for creating the databases/triggers
    private static final String CREATE_NOTES_TABLE = "CREATE TABLE " + TABLE_NOTES + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TITLE + " TEXT, " +
            COLUMN_NOTE + " TEXT, " +
            COLUMN_NOTE_MODIFIED + " INTEGER);";
    private static final String CREATE_TAGS_TABLE = "CREATE TABLE " + TABLE_TAGS + " (" +
            COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COLUMN_TAGS + " TEXT);";
    private static final String CREATE_TAGS_NOTES_TABLE = "CREATE TABLE " +
            TABLE_TAGS_NOTES + " (" +
            COLUMN_NOTES_ID + " INTEGER PRIMARY KEY, " +
            COLUMN_TAGS_ID + " INTEGER);";
    private static final String CREATE_VIRTUAL_NOTES_TABLE = "CREATE VIRTUAL TABLE " +
            VIRTUAL_TABLE_NOTES + " USING fts3" +
            "(content=\"notes\", " +
            COLUMN_TITLE + ", " +
            COLUMN_NOTE + ");";
    private static final String CREATE_TRIGGER_BU = "CREATE TRIGGER " +
            TRIGGER_BEFORE_UPDATE + " BEFORE UPDATE ON " +
            TABLE_NOTES + " BEGIN DELETE FROM " +
            VIRTUAL_TABLE_NOTES + " WHERE docid=old.rowid; END;";
    private static final String CREATE_TRIGGER_BD = "CREATE TRIGGER " +
            TRIGGER_BEFORE_DELETE + " BEFORE DELETE ON " +
            TABLE_NOTES + " BEGIN DELETE FROM " +
            VIRTUAL_TABLE_NOTES + " WHERE docid=old.rowid; END;";
    private static final String CREATE_TRIGGER_AU = "CREATE TRIGGER " +
            TRIGGER_AFTER_UPDATE + " AFTER UPDATE ON " +
            TABLE_NOTES + " BEGIN INSERT INTO " +
            VIRTUAL_TABLE_NOTES + "(docid, " +
            COLUMN_TITLE + ", " +
            COLUMN_NOTE + ") VALUES(new.rowid, new." +
            COLUMN_TITLE + ", new." +
            COLUMN_NOTE + "); END;";
    private static final String CREATE_TRIGGER_AI = "CREATE TRIGGER " +
            TRIGGER_AFTER_INSERT + " AFTER INSERT ON " +
            TABLE_NOTES + " BEGIN INSERT INTO " +
            VIRTUAL_TABLE_NOTES + "(docid, " +
            COLUMN_TITLE + ", " +
            COLUMN_NOTE + ") VALUES(new.rowid, new." +
            COLUMN_TITLE + ", new." +
            COLUMN_NOTE + "); END;";

    public DatabaseHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creates the database with beautiful sql syntax
        /* TODO add 2 tables, one to hold notebook/tag types, and 1 to hold note=tag */
        try {
            db.beginTransaction();
            // Main table to hold the notes, titles, date modified
            db.execSQL(CREATE_NOTES_TABLE);
            // Creates 2 tables, one to store tags, and one to store note/tag relation
            db.execSQL(CREATE_TAGS_TABLE);
            db.execSQL(CREATE_TAGS_NOTES_TABLE);
            // Virtual table that allows for faster full text searches, holding just notes + titles
            db.execSQL(CREATE_VIRTUAL_NOTES_TABLE);
            // Triggers before an update to the main table, to delete the data from the virtual table
            db.execSQL(CREATE_TRIGGER_BU);
            db.execSQL(CREATE_TRIGGER_BD);
            // Triggers after the update, to insert the new data into the virtual table
            db.execSQL(CREATE_TRIGGER_AU);
            db.execSQL(CREATE_TRIGGER_AI);
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
                try {
                    db.beginTransaction();
                    // Adds a column to record the last time the note was modified
                    db.execSQL("ALTER TABLE " +
                            TABLE_NOTES + " ADD COLUMN " +
                            COLUMN_NOTE_MODIFIED + " INTEGER");
                    // Creates 2 tables, one to store tags, and one to store note/tag relation
                    db.execSQL(CREATE_TAGS_TABLE);
                    db.execSQL(CREATE_TAGS_NOTES_TABLE);
                    // Virtual table that allows for faster full text searches, holding just notes + titles
                    db.execSQL(CREATE_VIRTUAL_NOTES_TABLE);
                    // To add all the current notes to the new virtual table
                    db.execSQL("INSERT INTO " + VIRTUAL_TABLE_NOTES + " (docid, " +
                            COLUMN_TITLE + ", " +
                            COLUMN_NOTE + ") SELECT " +
                            COLUMN_ID + ", " +
                            COLUMN_TITLE + ", " +
                            COLUMN_NOTE + " FROM " +
                            TABLE_NOTES + ";");
                    // Triggers before an update to the main table, to delete the data from the virtual table
                    db.execSQL(CREATE_TRIGGER_BU);
                    db.execSQL(CREATE_TRIGGER_BD);
                    // Triggers after the update, to insert the new data into the virtual table
                    db.execSQL(CREATE_TRIGGER_AU);
                    db.execSQL(CREATE_TRIGGER_AI);
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                break;
            case 2:
                try {
                    db.beginTransaction();
                    // Creates 2 tables, one to store tags, and one to store note/tag relation
                    db.execSQL(CREATE_TAGS_TABLE);
                    db.execSQL(CREATE_TAGS_NOTES_TABLE);
                    // Virtual table that allows for faster full text searches, holding just notes + titles
                    db.execSQL(CREATE_VIRTUAL_NOTES_TABLE);
                    // To add all the current notes to the new virtual table
                    db.execSQL("INSERT INTO " + VIRTUAL_TABLE_NOTES + " (docid, " +
                            COLUMN_TITLE + ", " +
                            COLUMN_NOTE + ") SELECT " +
                            COLUMN_ID + ", " +
                            COLUMN_TITLE + ", " +
                            COLUMN_NOTE + " FROM " +
                            TABLE_NOTES + ";");
                    // Triggers before an update to the main table, to delete the data from the virtual table
                    db.execSQL(CREATE_TRIGGER_BU);
                    db.execSQL(CREATE_TRIGGER_BD);
                    // Triggers after the update, to insert the new data into the virtual table
                    db.execSQL(CREATE_TRIGGER_AU);
                    db.execSQL(CREATE_TRIGGER_AI);
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
                break;
            case 3:
                try {
                    db.beginTransaction();
                    // Creates 2 tables, one to store tags, and one to store note/tag relation
                    db.execSQL(CREATE_TAGS_TABLE);
                    db.execSQL(CREATE_TAGS_NOTES_TABLE);
                    db.setTransactionSuccessful();
                }
                finally {
                    db.endTransaction();
                }
        }
    }
}