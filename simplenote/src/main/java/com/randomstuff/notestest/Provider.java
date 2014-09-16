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
import android.util.Log;

public class Provider extends ContentProvider {
    private DatabaseHelper db=null;

    // Used with the uri matcher, to allow switch statements based on URI type
    private static final int NOTES = 1;
    private static final int NOTE_ID = 2;
    private static final int VIRTUAL_NOTES = 3;
    private static final int VIRTUAL_NOTES_ID = 4;
    private static final int TAGS = 5;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes", NOTES);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes/#", NOTE_ID);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes_virtual", VIRTUAL_NOTES);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes_virtual/#", VIRTUAL_NOTES_ID);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "tags", TAGS);
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
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        Cursor c = null;

        int uriType = sURIMatcher.match(uri);

        switch (uriType) {
            case NOTES:
                qb = new SQLiteQueryBuilder();
                qb.setTables(NotesContract.Notes.TABLE_NAME);
                c = qb.query(db.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sort);
                break;
            case NOTE_ID:
                // Uses the query builder add a WHERE clause based on note _id, from the URI
                qb = new SQLiteQueryBuilder();
                qb.setTables(NotesContract.Notes.TABLE_NAME);
                qb.appendWhere(NotesContract.Notes.COLUMN_ID + "=" + uri.getLastPathSegment());

                c = qb.query(db.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sort);
                break;
            case VIRTUAL_NOTES:
                // This is used when searching for text, will return rows from notes
                // Using a rawquery here as the querybuilder seemed awkward for making a nested select

                String sql = "SELECT " + projection[0] + ", " + projection[1] + ", " +
                        projection[2] + ", " + projection[3] + " FROM " +
                        NotesContract.Notes.TABLE_NAME + " WHERE " +
                        NotesContract.Notes.COLUMN_ID + " IN (SELECT " +
                        NotesContract.NotesVirtual.COLUMN_ID + " FROM " +
                        NotesContract.NotesVirtual.TABLE_NAME +
                        " WHERE " + NotesContract.NotesVirtual.TABLE_NAME + " MATCH ?) ORDER BY " +
                        sort;
                c = db.getReadableDatabase().rawQuery(sql, selectionArgs);
                break;
            case VIRTUAL_NOTES_ID:
                qb = new SQLiteQueryBuilder();
                qb.setTables(NotesContract.NotesVirtual.TABLE_NAME);
                break;
            case TAGS:
                qb = new SQLiteQueryBuilder();
                qb.setTables(NotesContract.Tags.TABLE_NAME);
                c = qb.query(db.getReadableDatabase(), projection,
                        null, null, null, null, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }


        if (c != null) {
            c.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return c;

    }

    @Override
    synchronized public Uri insert(Uri uri, ContentValues cv) {
        long rowID = db.getWritableDatabase().insert(NotesContract.Notes.TABLE_NAME, null, cv);

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
        int count = db.getWritableDatabase().update(NotesContract.Notes.TABLE_NAME,
                cv,
                NotesContract.Notes.COLUMN_ID + "=" + noteId,
                selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    synchronized public int delete(Uri uri, String selection, String[] selectionArgs) {
        //could chagne this to allow delete multiple items, or database
        String noteId = uri.getLastPathSegment();
        int count = db.getWritableDatabase().delete(NotesContract.Notes.TABLE_NAME,
                NotesContract.Notes.COLUMN_ID + "=" + noteId ,
                selectionArgs);
        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}

