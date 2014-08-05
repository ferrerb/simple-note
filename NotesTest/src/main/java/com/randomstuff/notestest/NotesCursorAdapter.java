package com.randomstuff.notestest;

import android.content.Context;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/* Done with mucho help from Cyril Mottier's post on listview tips and tricks */
public class NotesCursorAdapter extends CursorAdapter {
    private static final int ROW_DEFAULT = 1;
    private static final int ROW_SEPARATOR = 2;
    private static final int ROW_UNKNOWN = 0;
    private int[] rowState;

    public NotesCursorAdapter(Context ctxt, Cursor c, int flags) {
        super(ctxt, c, flags);
        rowState = (c == null) ? null : new int[c.getCount()];
        //this.registerDataSetObserver(dataObserver);
    }

    /* The viewholder lets you avoid calling findViewById for every bindView, and hold onto
    *  the resource as a parameter
    */
    private static class CursorViewHolder {
        public TextView separator;
        public TextView titleView;
        public TextView noteView;
    }

    private final DataSetObserver dataObserver = new DataSetObserver() {
        @Override
        public void onChanged() {
            super.onChanged();
            updateStateCache();
        }

        @Override
        public void onInvalidated() {
            super.onInvalidated();
            updateStateCache();
        }
    };

    private synchronized void updateStateCache() {
        rowState = null;
    }

    @Override
    public void changeCursor(Cursor c) {
        super.changeCursor(c);
        rowState = (c == null) ? null : new int[c.getCount()];
    }

    @Override
    public void bindView(View view, Context ctxt, Cursor c) {
        final CursorViewHolder holder = (CursorViewHolder) view.getTag();
        boolean needSeparator = false;
        final int position = c.getPosition();

        long dateModLong = c.getLong(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE_MODIFIED));
        String dateMod = DateFormat.format("LLLL yyyy", dateModLong).toString();

        switch (rowState[position]) {
            case ROW_SEPARATOR:
                needSeparator = true;
                break;
            case ROW_DEFAULT:
                needSeparator = false;
                break;
            case ROW_UNKNOWN:
            default:
                String dateModPrev = null;
                if (c.getPosition() != 0) {
                    c.moveToPosition(position - 1);
                    long dateModLongPrev = c.getLong(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE_MODIFIED));
                    dateModPrev = DateFormat.format("LLLL yyyy", dateModLongPrev).toString();
                    c.moveToPosition(position);
                }
                if (position == 0) {
                    needSeparator = true;
                } else if (!dateMod.equals(dateModPrev)){
                    needSeparator = true;
                } else {
                    needSeparator = false;
                }
                rowState[position] = needSeparator ? ROW_SEPARATOR : ROW_DEFAULT;
                break;
        }

        if (needSeparator) {
            holder.separator.setText(dateMod);
            holder.separator.setVisibility(View.VISIBLE);
        } else {
            holder.separator.setVisibility(View.GONE);
        }

        holder.titleView.setText(c.getString(c.getColumnIndex(NotesContract.Notes.COLUMN_TITLE)));
        holder.noteView.setText(c.getString(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE)));

    }

    @Override
    public View newView(Context ctxt, Cursor cursor, ViewGroup parent) {
        View v = LayoutInflater.from(ctxt).inflate(R.layout.simple_list_item_3, parent, false);


        CursorViewHolder holder = new CursorViewHolder();
        holder.separator = (TextView) v.findViewById(R.id.month_separator);
        holder.titleView = (TextView) v.findViewById(R.id.list_note_title);
        holder.noteView = (TextView) v.findViewById(R.id.list_note_body);

        v.setTag(holder);
        return v;
    }
}
