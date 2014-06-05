package com.randomstuff.notestest;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="notestest.db";
    private static final int SCHEMA_VERSION=1;
    private Context ctxt=null;

    private DatabaseHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
        this.ctxt=ctxt;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //create the database
        try{
            db.beginTransaction();
            db.execSQL("CREATE TABLE notes (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                       "title TEXT, note TEXT);");
            db.setTransactionSuccessful();
        }
        finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //tell it that if its upgrading, monkeys in the system
        throw new RuntimeException(ctxt.getString(R.string.sql_upgrade_error));
    }

    private class GetNoteTask extends AsyncTask<Integer, Void, String>{
        //create, or retrieve a note

        protected Void doInBackground(){

        }
    }

    private class SaveNoteTask extends AsyncTask<Void, Void, Void> {
        //save the note

        protected void doInBackground(){

        }
    }

    private class DeleteNoteTask extends AsyncTask<Integer, Void, Void> {
        //deletetetete

        protected void doInBackground() {

        }
    }

    void getNoteAsync(){

    }

    void saveNoteAsync(){

    }

    void deleteNoteAsync(){

    }

}
