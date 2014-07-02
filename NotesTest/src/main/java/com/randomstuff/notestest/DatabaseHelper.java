package com.randomstuff.notestest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "notestest.db";
    private static final int SCHEMA_VERSION = 1;
    private Context ctxt=null;

    private static final String TABLE_NAME ="notes";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_TITLE = "title";
    private static final String COLUMN_NOTE = "note";

    public DatabaseHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //creates the database with beautiful sql syntax
        //consider making a more complex table, possibly with date modified/created
        try {
            db.beginTransaction();
            db.execSQL("CREATE TABLE " + TABLE_NAME + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_TITLE + " TEXT, " +
                    COLUMN_NOTE + " TEXT);");
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //tell it that if its upgrading, monkeys in the system
        throw new RuntimeException(ctxt.getString(R.string.sql_upgrade_error));
    }
}