package com.randomstuff.notestest;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class Provider extends ContentProvider {
    private DatabaseHelper db=null;

    private static final String DATABASE_NAME = "notes";
    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;
    private static final String AUTHORITY = "com.randomstuff.notestest.Provider";
    private static final String BASE_PATH = "notes";

    public static final class Constants implements BaseColumns {
        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NOTE = "note";
        public static final String COLUMN_NOTE_MODIFIED = "note_mod";
        public static final Uri CONTENT_URI =
                Uri.parse("content://" + AUTHORITY + "/" + DATABASE_NAME);
    }

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(AUTHORITY, BASE_PATH, NOTES);
        sURIMatcher.addURI(AUTHORITY, BASE_PATH + "/#", NOTE_ID);
    }

    public boolean onCreate() {
        db= new DatabaseHelper(getContext());
        return (db != null);
    }

    @Override
    synchronized public String getType(Uri uri) {
        /// implement uri matcher
        return null;
    }

    @Override
    synchronized public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sort) {
        //query!
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DATABASE_NAME);

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case NOTES:
                break;
            case NOTE_ID:
                qb.appendWhere(Provider.Constants.COLUMN_ID + "=" + uri.getLastPathSegment());
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }

        Cursor c = qb.query(db.getReadableDatabase(), projection,
                            selection, selectionArgs, null, null, sort);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return c;

    }

    @Override
    synchronized public Uri insert(Uri uri, ContentValues cv) {
        long rowID = db.getWritableDatabase().insert(DATABASE_NAME, null, cv);

        if (rowID > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, rowID);
            getContext().getContentResolver().notifyChange(uri, null);
            return newUri;
        }
        else {
            throw new SQLException("Failed to insert row into " + uri);
        }

    }

    @Override
    synchronized public int update(Uri uri, ContentValues cv, String selection,
                                   String[] selectionArgs) {
        String noteId = uri.getLastPathSegment();
        int count = db.getWritableDatabase().update(DATABASE_NAME,
                cv,
                Provider.Constants.COLUMN_ID + "=" + noteId,
                selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    synchronized public int delete(Uri uri, String selection, String[] selectionArgs) {
        //could chagne this to allow delete multiple items, or database
        String noteId = uri.getLastPathSegment();
        int count = db.getWritableDatabase().delete(DATABASE_NAME,
                Provider.Constants.COLUMN_ID + "=" + noteId ,
                selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}

