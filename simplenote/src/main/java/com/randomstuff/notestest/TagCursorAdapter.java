package com.randomstuff.notestest;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageButton;
import android.widget.TextView;

import com.randomstuff.notestest.data.NotesContract;

/** A custom cursor adapter to add a button for each tag, allowing the user
 * to delete the tag.
 */
public class TagCursorAdapter extends CursorAdapter {
    private OnTagDeleteListener mTagCallbacks;

    public TagCursorAdapter(Context ctxt, Cursor cursor,
                            int flags, OnTagDeleteListener mTagCallbacks) {
        super(ctxt, cursor, flags);
        this.mTagCallbacks = mTagCallbacks;
    }

    /** Holds references to the views in each row, and the position in the listview */
    public static class CursorViewHolder {
        public TextView mNoteTag;
        public ImageButton mDeleteTag;
        int mViewPosition;
    }

    /** Sends the position of the tag to be deleted in the list view, as well as whether to delete
     *  all notes with that tag.
     */
    public interface OnTagDeleteListener {
        public void onDeleteTag(int position, int deleteNotes);
    }

    @Override
    public void bindView(View v, Context c, Cursor cursor) {
        final CursorViewHolder holder = (CursorViewHolder) v.getTag();
        holder.mNoteTag.setText(cursor.getString(
                cursor.getColumnIndex(NotesContract.Tags.COLUMN_TAGS)));

        holder.mDeleteTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("holder view id cursor column", Integer.toString(holder.mViewPosition));

                AlertDialog.Builder deleteTagDialog = new AlertDialog.Builder(v.getContext());
                deleteTagDialog.setTitle(R.string.delete_selected_tag)
                        .setItems(R.array.tag_delete_dialog, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do something based on the dialog choice, which = array item
                                if (which == 0) {
                                    mTagCallbacks.onDeleteTag(holder.mViewPosition, 0);
                                }
                                if (which == 1) {
                                    // delete the tag, and the notes tagged with it
                                    mTagCallbacks.onDeleteTag(holder.mViewPosition, 1);
                                }
                            }
                        })
                        .show();
            }
        });
    }

    @Override
    public View newView(Context c, Cursor cursor, ViewGroup group) {
        View v = LayoutInflater.from(c).inflate(R.layout.list_item_with_delete, group, false);

        CursorViewHolder holder = new CursorViewHolder();
        holder.mNoteTag = (TextView) v.findViewById(R.id.tag_view_list_item);
        holder.mDeleteTag = (ImageButton) v.findViewById(R.id.delete_tag_btn);
        holder.mViewPosition = cursor.getPosition();
        v.setTag(holder);
        return v;
    }

}
