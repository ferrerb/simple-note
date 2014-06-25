package com.randomstuff.notestest;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;

public class Provider extends ContentProvider {
    private DatabaseHelper db=null;

    private static final String DATABASE_NAME = "notes";

    public static final class Constants implements BaseColumns {
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_NOTE = "note";
    }

    public boolean onCreate() {
        db= new DatabaseHelper(getContext());
        return ((db != null));
    }

    @Override
    public String getType(Uri uri) {
        /// implement uri matcher
        if (isCollectionUri(uri)) {

        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                      String[] selectionArgs, String sort) {
        //query!
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        qb.setTables(DATABASE_NAME);

        Cursor c = qb.query(db.getReadableDatabase(), projection, selection, selectionArgs, null, null, sort);
        c.setNotificationUri(getContext().getContentResolver(), uri);
        return(c);

    }

    @Override
    public Uri insert(Uri uri, ContentValues cv) {
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

}

