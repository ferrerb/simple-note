package com.randomstuff.notestest;

import android.content.ContentValues;
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

    //interface/callback with NoteListFragment, sets the note list
    interface ListListener {
        void setList(Cursor noteList);
    }

    //interface/callback with NoteFragment, sets the edittext to the note
    interface NoteListener {
        void setNote(String[] note);
    }

    private class GetNoteList extends AsyncTask<Void, Void, Cursor> {
        /*get the list of titles preferably, and you need a interface like NoteListener
        * just like in GetNoteTask
        */
        private ListListener listener1=null;

        GetNoteList(ListListener listener1) {
            this.listener1=listener1;
        }

        @Override
        protected Cursor doInBackground(Void... params) {
            Cursor listOfNotes = getReadableDatabase().query("notes",
                    new String[]{"_id", "title"},
                    null, null, null, null, null);

            if (listOfNotes.isAfterLast()) {
                return(null);
            }

            return(listOfNotes);
        }

        @Override
        public void onPostExecute(Cursor mList) {
            listener1.setList(mList);
        }
    }

    private class GetNoteTask extends AsyncTask<Integer, Void, String[]>{
        //retrieve a note
        private NoteListener listener=null;

        GetNoteTask(NoteListener listener) {
            this.listener=listener;
        }

        @Override
        protected String[] doInBackground(Integer... params){
            String args= params[0].toString();
            Cursor c = getReadableDatabase().query("notes",
                    new String[]{"_id", "title", "note"},
                    args, null, null, null, null);

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

    private class SaveNoteTask extends AsyncTask<Void, Void, Void> {
        //save the note
        private int position;
        private String title=null;
        private String body=null;

        SaveNoteTask(int position, String title, String body) {
            this.position=position;
            this.title=title;
            this.body=body;
        }

        @Override
        protected Void doInBackground(Void... params){
            //might need some logic if null
            if (position == -1) {
                ContentValues cv = new ContentValues();
                cv.put("title", title);
                cv.put("note", body);
                getWritableDatabase().insert("notes", null, cv);
            }
            else {
                ContentValues cv = new ContentValues();
                cv.put("title", title);
                cv.put("note", body);
                getWritableDatabase().update("notes", cv, "_id = " + String.valueOf(position), null);
            }
            return(null);
        }
    }

    private class DeleteNoteTask extends AsyncTask<Integer, Void, Void> {
        //deletetetete
        @Override
        protected Void doInBackground(Integer... params) {
            return(null);
        }
    }

    void getListAsync(ListListener listener1) {
        new GetNoteList(listener1).execute();
    }

    void getNoteAsync(int position, NoteListener listener){
        new GetNoteTask(listener).execute(position);
    }

    void saveNoteAsync(int position, String title, String body){
        new SaveNoteTask(position, title, body).execute();
    }

    void deleteNoteAsync(){

    }

}
