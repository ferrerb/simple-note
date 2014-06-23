package com.randomstuff.notestest;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.AsyncTask;
import android.util.Log;

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
                    null, null, null, null, "_id DESC");

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

    private class GetNoteTask extends AsyncTask<Long, Void, String[]>{
        //retrieve a note
        private NoteListener listener=null;

        GetNoteTask(NoteListener listener) {
            this.listener=listener;
        }

        @Override
        protected String[] doInBackground(Long... params){
            String args= params[0].toString();
            String[] mNote = new String[2];

            Log.d("index passed", args);

            Cursor c = getReadableDatabase().query("notes",
                    new String[] {"_id", "title", "note"},
                    "_id=" + args, null, null, null, null);

            if (c != null && c.moveToFirst()) {
                mNote[0] = c.getString(1);
                mNote[1] = c.getString(2);

                c.close();
            }

            //Log.d("test", c.getString(0));
            //Log.d("title", c.getString(1));
            //Log.d("note", c.getString(2));



            return(mNote);

        }

        @Override
        public void onPostExecute(String[] stuff){
            listener.setNote(stuff);
        }
    }

    private class SaveNoteTask extends AsyncTask<Void, Void, Void> {
        //save the note
        private long position;
        private String title=null;
        private String body=null;

        SaveNoteTask(long position, String title, String body) {
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
                //check this to see if it writes to position 0 or something
                ContentValues cv = new ContentValues();
                cv.put("title", title);
                cv.put("note", body);
                getWritableDatabase().update("notes", cv, "_id = " + String.valueOf(position), null);
            }
            return(null);
        }
    }

    private class DeleteNoteTask extends AsyncTask<Long, Void, Void> {
        //deletetetete
        @Override
        protected Void doInBackground(Long... params) {
            String args = params[0].toString();
            getWritableDatabase().delete("notes", "id=" + args, null);
            return(null);
        }
    }

    void getListAsync(ListListener listener1) {
        new GetNoteList(listener1).execute();
    }

    void getNoteAsync(long position, NoteListener listener){
        new GetNoteTask(listener).execute(position);
    }

    void saveNoteAsync(long position, String title, String body){
        new SaveNoteTask(position, title, body).execute();
    }

    void deleteNoteAsync(long position){
        new DeleteNoteTask().execute(position);
    }

}
