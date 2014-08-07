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

/* Done with mucho help from Cyril Mottier's post on listview tips and tricks
 * This is an adapter that separates the cursor data by month
 */
public class SeparatorCursorAdapter extends CursorAdapter {
    /* These are stored in an int array against the cursor position
       to cache whether the view should have a separator visible
     */
    private static final int ROW_DEFAULT = 1;
    private static final int ROW_SEPARATOR = 2;
    private static final int ROW_UNKNOWN = 0;
    private int[] rowState;

    public SeparatorCursorAdapter(Context ctxt, Cursor c, int flags) {
        super(ctxt, c, flags);
        /* Sets the cache to null if cursor is null, or the size of the cursor */
        rowState = (c == null) ? null : new int[c.getCount()];
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
    public Cursor swapCursor(Cursor c) {
         /* Sets the cache to null to make sure the data is cleared when the cursor is changed.
          * Then, sets rowState to the size of the not null cursor */
        rowState = null;
        if (c != null) { rowState = new int[c.getCount()]; }

        return super.swapCursor(c);
    }

    @Override
    public void bindView(View view, Context ctxt, Cursor c) {
        final CursorViewHolder holder = (CursorViewHolder) view.getTag();
        boolean needSeparator;
        final int position = c.getPosition();
        /* Storing the date modified for the current cursor row
         * to check if it differs from the previous row
         */
        long dateModLong = c.getLong(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE_MODIFIED));
        String dateMod = DateFormat.format("LLLL yyyy", dateModLong).toString();

        // Checks the cache to decide whether a separator is needed
        switch (rowState[position]) {
            case ROW_SEPARATOR:
                needSeparator = true;
                break;
            case ROW_DEFAULT:
                needSeparator = false;
                break;
            case ROW_UNKNOWN:
            default:
                // Checks the current and previous cursor position dates as long as position > 0
                String dateModPrev = null;
                if (c.getPosition() != 0) {
                    c.moveToPosition(position - 1);
                    long dateModLongPrev = c.getLong(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE_MODIFIED));
                    dateModPrev = DateFormat.format("LLLL yyyy", dateModLongPrev).toString();
                    c.moveToPosition(position);
                }
                needSeparator = (position == 0) || (!dateMod.equals(dateModPrev));

                rowState[position] = needSeparator ? ROW_SEPARATOR : ROW_DEFAULT;
                break;
        }

        // Sets the separator view as visible
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
