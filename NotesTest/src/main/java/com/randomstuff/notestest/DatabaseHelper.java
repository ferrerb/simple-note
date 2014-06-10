package com.randomstuff.notestest;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME="notestest.db";
    private static final int SCHEMA_VERSION=1;
    private static DatabaseHelper singleton=null;
    private Context ctxt=null;

    private DatabaseHelper(Context ctxt) {
        super(ctxt, DATABASE_NAME, null, SCHEMA_VERSION);
        this.ctxt=ctxt;
    }

    synchronized static DatabaseHelper getInstance(Context ctxt){
        if (singleton==null){
            singleton = new DatabaseHelper(ctxt.getApplicationContext());
        }
        return(singleton);
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

    //interface/callback with NoteFragment, sets the edittext to the note
    interface NoteListener {
        void setNote(String[] note);
    }

    private class GetNoteList extends AsyncTask<Void, Void, >

    private class GetNoteTask extends AsyncTask<Integer, Void, String[]>{
        //retrieve a note
        private NoteListener listener=null;

        GetNoteTask(NoteListener listener) {
            this.listener=listener;
        }

        @Override
        protected String[] doInBackground(Integer... params){
            //String[] args= {params[0].toString() };
            Cursor c = getReadableDatabase().rawQuery("SELECT title, note " +
                                                      "FROM notes WHERE position=?", null);
            c.moveToFirst();

            if (c.isAfterLast()) {
                return(null);
            }

            String[] note = new String[2];
            note[0] = c.getString(1);
            note[1] = c.getString(2);

            c.close();

            return(note);

        }

        @Override
        public void onPostExecute(String[] stuff){
            listener.setNote(stuff);
        }
    }

    //create a note
    private class CreateNoteTask extends AsyncTask<Void, Void, Void> {

    }

    private class SaveNoteTask extends AsyncTask<Void, Void, Void> {
        //save the note

        @Override
        protected void doInBackground(){

        }
    }

    private class DeleteNoteTask extends AsyncTask<Integer, Void, Void> {
        //deletetetete
        @Override
        protected Void doInBackground() {

        }
    }

    void getNoteAsync(int position, NoteListener listener){
        new GetNoteTask(listener).execute(position);
    }

    void saveNoteAsync(){

    }

    void deleteNoteAsync(){

    }

}
