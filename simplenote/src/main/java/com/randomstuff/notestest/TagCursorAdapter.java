package com.randomstuff.notestest;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

/**
 * A custom cursor adapter to add a button for each tag, allowing the user
 * to delete the tag.
 */
public class TagCursorAdapter extends CursorAdapter implements View.OnClickListener {

    public TagCursorAdapter(Context ctxt, Cursor cursor, int flags) {
        super(ctxt, cursor, flags);
    }

    public static class CursorViewHolder {
        public TextView noteTag;
        public ImageButton deleteTag;
    }

    @Override
    public void bindView(View v, Context c, Cursor cursor) {
        final CursorViewHolder holder = (CursorViewHolder) v.getTag();
        holder.noteTag.setText(cursor.getString(
                cursor.getColumnIndex(NotesContract.Tags.COLUMN_TAGS)));
        holder.noteTag.setOnClickListener(this);
        holder.deleteTag.setOnClickListener(this);
    }

    @Override
    public View newView(Context c, Cursor cursor, ViewGroup group) {
        View v = LayoutInflater.from(c).inflate(R.layout.list_item_with_delete, group, false);

        CursorViewHolder holder = new CursorViewHolder();
        holder.noteTag = (TextView) v.findViewById(R.id.tag_view_list_item);
        holder.deleteTag = (ImageButton) v.findViewById(R.id.delete_tag_btn);
        v.setTag(holder);
        return v;
    }

    @Override
    public void onClick(View v) {
        // do on click things
        
    }
}
