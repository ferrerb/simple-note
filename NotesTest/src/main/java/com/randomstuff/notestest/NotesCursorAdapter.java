package com.randomstuff.notestest;

import android.content.Context;
import android.database.Cursor;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

/* Done with mucho help from Cyril Mottier's post on listview tips and tricks */
public class NotesCursorAdapter extends CursorAdapter {

    private String dateModPrev = null;
    public NotesCursorAdapter(Context ctxt, Cursor c, int flags) {
        super(ctxt, c, flags);
    }

    /* The viewholder lets you avoid calling findViewById for every bindView, and hold onto
    *  the resource as a parameter
    */
    private static class CursorViewHolder {
        public TextView separator;
        public TextView titleView;
        public TextView noteView;
    }

    @Override
    public void bindView(View view, Context ctxt, Cursor c) {
        final CursorViewHolder holder = (CursorViewHolder) view.getTag();
        boolean needSeparator = false;
        final int position = c.getPosition();
        Log.d("position", String.valueOf(position));

        long dateModLong = c.getLong(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE_MODIFIED));
        String dateMod = DateFormat.format("LLLL yyyy", dateModLong).toString();

        if (!dateMod.equals(dateModPrev)) {
            needSeparator = true;
            dateModPrev = dateMod;
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
