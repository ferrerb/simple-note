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
    private static final int TAGS_ID = 6;
    private static final int TAGS_NOTES = 7;
    private static final int TAGS_NOTES_ID = 8;

    private static final UriMatcher sURIMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static {
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes", NOTES);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes/#", NOTE_ID);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes_virtual", VIRTUAL_NOTES);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "notes_virtual/#", VIRTUAL_NOTES_ID);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "tags", TAGS);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "tags/#", TAGS_ID);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "tags_notes", TAGS_NOTES);
        sURIMatcher.addURI(NotesContract.AUTHORITY, "tags_notes/#", TAGS_NOTES_ID);
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
        SQLiteQueryBuilder qb;

        Cursor c;

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
                String sqlNote = "SELECT " + projection[0] + ", " + projection[1] + ", " +
                        projection[2] + ", " + projection[3] + ", " + projection[4] + " FROM " +
                        NotesContract.Notes.TABLE_NAME +
                        " LEFT JOIN " +
                        NotesContract.Tags_Notes.TABLE_NAME +
                        " ON (" +
                        NotesContract.Notes.TABLE_NAME + "." + NotesContract.Notes.COLUMN_ID + "=" +
                        NotesContract.Tags_Notes.TABLE_NAME + "." + NotesContract.Tags_Notes.COLUMN_NOTES_ID +
                        ") LEFT JOIN " +
                        NotesContract.Tags.TABLE_NAME +
                        " ON (" +
                        NotesContract.Tags_Notes.TABLE_NAME + "." + NotesContract.Tags_Notes.COLUMN_TAGS_ID + "=" +
                        NotesContract.Tags.TABLE_NAME + "." + NotesContract.Tags.COLUMN_ID + ") WHERE " +
                        NotesContract.Notes.TABLE_NAME + "." + NotesContract.Notes.COLUMN_ID + "=?";
                qb = new SQLiteQueryBuilder();
                qb.setTables(NotesContract.Notes.TABLE_NAME);
                qb.appendWhere(NotesContract.Notes.COLUMN_ID + "=" + uri.getLastPathSegment());

                c = db.getReadableDatabase().rawQuery(sqlNote, selectionArgs);
                break;
            case VIRTUAL_NOTES:
                // This is used when searching for text, will return rows from notes
                // Using a rawquery here as the querybuilder seemed awkward for making a nested select

                String sql = "SELECT " + projection[0] + ", " + projection[1] + ", " +
                        projection[2] + ", " + projection[3] + " FROM " +
                        NotesContract.Notes.TABLE_NAME + " WHERE " +
                        NotesContract.Notes.COLUMN_ID + " IN (SELECT " +
                        NotesContract.NotesVirtual.COLUMN_ID + " FROM " +
                        NotesContract.NotesVirtual.TABLE_NAME + " WHERE " +
                        NotesContract.NotesVirtual.TABLE_NAME + " MATCH ?) ORDER BY " +
                        sort;
                c = db.getReadableDatabase().rawQuery(sql, selectionArgs);
                break;
            case TAGS:
                Log.d("uri from tags", uri.toString());
                qb = new SQLiteQueryBuilder();
                qb.setTables(NotesContract.Tags.TABLE_NAME);
                c = qb.query(db.getReadableDatabase(), projection,
                        selection, selectionArgs, null, null, sort);
                break;
            case TAGS_NOTES:
                // This uri is for selecting all notes with a specific tag
                String sqlTags = "SELECT " + projection[0] + ", " + projection[1] + ", " +
                        projection[2] + ", " + projection[3] + " FROM " +
                        NotesContract.Notes.TABLE_NAME + " WHERE " +
                        NotesContract.Notes.COLUMN_ID + " IN (SELECT " +
                        NotesContract.Tags_Notes.COLUMN_NOTES_ID + " FROM " +
                        NotesContract.Tags_Notes.TABLE_NAME + " WHERE " +
                        NotesContract.Tags_Notes.COLUMN_TAGS_ID + " = ?) ORDER BY " +
                        sort;
                c = db.getReadableDatabase().rawQuery(sqlTags, selectionArgs);
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
        long rowId;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TAGS:
                rowId = db.getWritableDatabase().insert(NotesContract.Tags.TABLE_NAME, null, cv);
                break;
            case NOTES:
                rowId = db.getWritableDatabase().insert(NotesContract.Notes.TABLE_NAME, null, cv);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);

        }

        if (rowId > 0) {
            Uri newUri = ContentUris.withAppendedId(uri, rowId);
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
        int count;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case NOTE_ID:
                String noteId = uri.getLastPathSegment();
                count = db.getWritableDatabase().update(NotesContract.Notes.TABLE_NAME,
                        cv,
                        NotesContract.Notes.COLUMN_ID + "=" + noteId,
                        selectionArgs);
                break;
            case TAGS_NOTES:
                count = db.getWritableDatabase().update(NotesContract.Tags_Notes.TABLE_NAME,
                        cv,
                        selection,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI : " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }

    @Override
    synchronized public int delete(Uri uri, String selection, String[] selectionArgs) {
        int count;
        String noteId;
        int uriType = sURIMatcher.match(uri);
        switch (uriType) {
            case TAGS_NOTES_ID:
                count = db.getWritableDatabase().delete(NotesContract.Tags_Notes.TABLE_NAME,
                        NotesContract.Tags_Notes.COLUMN_NOTES_ID + "=?",
                        selectionArgs);
                break;
            case NOTE_ID:
                noteId = uri.getLastPathSegment();
                count = db.getWritableDatabase().delete(NotesContract.Notes.TABLE_NAME,
                        NotesContract.Notes.COLUMN_ID + "=" + noteId ,
                        selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri : " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return count;
    }
}

