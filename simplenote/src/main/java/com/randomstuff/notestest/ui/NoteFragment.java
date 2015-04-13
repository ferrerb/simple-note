package com.randomstuff.notestest.ui;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.randomstuff.notestest.MainActivity;
import com.randomstuff.notestest.R;
import com.randomstuff.notestest.data.NotesContract;

/** Displays notes */
public class NoteFragment extends Fragment implements TagDialogFragment.TagDialogCallbacks {
    // Variables to hold references to the views associated with the note fragment
    private EditText mEditTitle = null;
    private EditText mEditNote = null;
    private TextView mTextDateModified = null;
    private Button mTagsBtn;

    private long mCurrentNoteId;
    private String mCurrentTag;
    private long mCurrentTagId = 0L;
    private boolean mIsDeleted = false;
    private boolean mIsChanged = false;

    private boolean mDualPane;
    private Uri mNoteUri = null;
    // Tokens to help identify the various async tasks used to deal with notes
    private static final int NOTE_INSERT_TOKEN = 1;
    private static final int NOTE_UPDATE_TOKEN = 2;
    private static final int NOTE_QUERY_TOKEN = 3;
    private static final int NOTE_DELETE_TOKEN = 4;
    private static final int TAG_INSERT_TOKEN = 5;
    private static final int NOTE_TAG_DELETE_TOKEN = 6;

    private static final String DATE_TIME_FORMAT = "h:mm a, LLL d";

    /** Returns a note fragment with the requested note id as a bundle */
    public static NoteFragment newInstance(long id) {
        NoteFragment mFrag = new NoteFragment();
        Log.d("id", String.valueOf(id));


        Bundle args = new Bundle();
        args.putLong("id", id);
        mFrag.setArguments(args);

        return mFrag;
    }

    /** Returns a note fragment with the shared text, and an id of -1 to indicate there is shared text */
    public static NoteFragment newInstance(long id, String shareText) {
        NoteFragment frag = new NoteFragment();
        Log.d("id", String.valueOf(id));


        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("share", shareText);
        frag.setArguments(args);

        return frag;
    }

    /** Retrieves the id from the bundle associated with this fragment. 0 is used as default since
     *  SQLite starts incrementing from 1.
     * @return long
     */
    public long getShownId() {
        return getArguments().getLong("id", 0L);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mCurrentNoteId = getShownId();
        if ( mCurrentNoteId > 0L) {
            mNoteUri = Uri.parse(NotesContract.Notes.CONTENT_URI + "/" + mCurrentNoteId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.notes_detail, container, false);

        View notesListFrame = getActivity().findViewById(R.id.notes_list);
        mDualPane = (notesListFrame != null) && (notesListFrame.getVisibility() == View.VISIBLE);
        // Find all the views in the layout and hold a reference to them
        mEditTitle = (EditText) result.findViewById(R.id.edit_title);
        mEditNote = (EditText) result.findViewById(R.id.edit_note);
        mTextDateModified = (TextView) result.findViewById(R.id.text_date_modified);
        mTagsBtn = (Button) result.findViewById(R.id.choose_tag_btn);

        // Fills the views with the requested note information, and adds a note listener to detect changes
        if (mNoteUri != null) {
            fillNote(mNoteUri);
        } else if (mCurrentNoteId == -1L) {
            mEditNote.setText(getArguments().getString("share"));
            mEditTitle.addTextChangedListener(noteChangedListener);
            mEditNote.addTextChangedListener(noteChangedListener);
        } else {
            mEditTitle.addTextChangedListener(noteChangedListener);
            mEditNote.addTextChangedListener(noteChangedListener);
        }

        setHasOptionsMenu(true);
        // Watches for button press, and creates the dialog fragment for choosing a tag
        mTagsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                TagDialogFragment frag = TagDialogFragment.newInstance(mCurrentTagId);
                frag.setTargetFragment(NoteFragment.this, 0);
                frag.show(fm, "add_tag");
            }
        });
        // Sets the home button in the actionbar as enabled if we are in single pane mode
        if (!mDualPane) {
            ((ActionBarActivity)getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        return (result);
    }

    /** Manipulates tag information for the current note based on choices in the tag dialog fragment */
    @Override
    public void onTagChosen(String tag, long id) {
        // If id is -1, that means the user created a new tag
        if (id == -1L) {
            mIsChanged = true;
            mCurrentTag = tag;
            mTagsBtn.setText(mCurrentTag);

            ContentValues cv = new ContentValues();
            cv.put(NotesContract.Tags.COLUMN_TAGS, tag);
            NoteAsyncQueryHandler mHandle =
                    new NoteAsyncQueryHandler(getActivity().getContentResolver());
            mHandle.startInsert(TAG_INSERT_TOKEN, null, NotesContract.Tags.CONTENT_URI, cv);

        }
        // If the id is more than 0, the tag already exists. Tagid will be saved on note save
        if (id > 0L) {
            mIsChanged = true;
            mCurrentTag = tag;
            mCurrentTagId = id;
            mTagsBtn.setText(mCurrentTag);
        }
        // If -2 is passed as the id, it means to remove the tag from the current note
        if (id == -2L && mCurrentTag.length() > 0) {
            NoteAsyncQueryHandler mHandle =
                    new NoteAsyncQueryHandler(getActivity().getContentResolver());
            mHandle.startDelete(
                    NOTE_TAG_DELETE_TOKEN,
                    null,
                    NotesContract.Tags_Notes.CONTENT_URI,
                    null,
                    new String[]{ Long.toString(mCurrentNoteId) });
            mIsChanged = true;
            mCurrentTag = null;
            mCurrentTagId = 0L;
            mTagsBtn.setText(R.string.choose_tag);
        }


    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notes_detail, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //maybe add confirm button in portrait mode
        switch (item.getItemId()){
            //Should only be enabled if in portrait mode
            case (android.R.id.home):
                Intent i = new Intent(getActivity(), MainActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                getActivity().finish();
                return true;
            case (R.id.share_button):
                setActionShareIntent();
                return true;
            case (R.id.delete):
                deleteNoteDialog();

                return true;
        }

        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onPause() {
        mEditTitle.removeTextChangedListener(noteChangedListener);
        mEditNote.removeTextChangedListener(noteChangedListener);
        if (!mIsDeleted) {
            saveNote();
        }

        super.onPause();
    }

    /** Creates a new intent to share the displayed note body only */
    public void setActionShareIntent() {
        Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, mEditNote.getText().toString());
        startActivity(Intent.createChooser(shareIntent,
                getResources().getString(R.string.share_choose)));

    }

    /** Monitors an EditText and then sets a boolean to true to indicate the note was changed */
    private TextWatcher noteChangedListener = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                mIsChanged = true;
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // nothing
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int after) {
                // nothing
            }
        };
    /** Begins a query to the content provider, using an async task, to display the requested note */
    private void fillNote(Uri uri) {
        String[] selectionArgs = new String[]{ uri.getLastPathSegment() };
        String[] projection = {
                (NotesContract.Notes.TABLE_NAME + "." + NotesContract.Notes.COLUMN_ID),
                NotesContract.Notes.TABLE_NAME + "." + NotesContract.Notes.COLUMN_TITLE,
                NotesContract.Notes.TABLE_NAME + "." + NotesContract.Notes.COLUMN_NOTE,
                NotesContract.Notes.TABLE_NAME + "." + NotesContract.Notes.COLUMN_NOTE_MODIFIED,
                NotesContract.Tags.TABLE_NAME + "." + NotesContract.Tags.COLUMN_ID,
                NotesContract.Tags.TABLE_NAME + "." + NotesContract.Tags.COLUMN_TAGS };

        NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                getContentResolver());
        mHandle.startQuery(NOTE_QUERY_TOKEN, null, uri, projection, null, selectionArgs, null);
    }

    /** Saves the current note. The tag information is also sent to the content provider, which
     *  will then decide whether there actually is a tag to save
     */
    private void saveNote() {
        boolean titleEmpty = mEditTitle.getText().toString().isEmpty();
        boolean noteEmpty = mEditNote.getText().toString().isEmpty();

        if ((!titleEmpty || !noteEmpty) && mNoteUri != null && mIsChanged) {
            ContentValues cv = new ContentValues();
            cv.put(NotesContract.Notes.COLUMN_TITLE, mEditTitle.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE, mEditNote.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());
            cv.put(NotesContract.Tags_Notes.COLUMN_TAGS_ID, mCurrentTagId);

            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startUpdate(NOTE_UPDATE_TOKEN, null, mNoteUri, cv, null, null);
        }
        if ((!titleEmpty || !noteEmpty) && mNoteUri == null) {
            ContentValues cv = new ContentValues();
            cv.put(NotesContract.Notes.COLUMN_TITLE, mEditTitle.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE, mEditNote.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());
            cv.put(NotesContract.Tags_Notes.COLUMN_TAGS_ID, mCurrentTagId);

            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startInsert(NOTE_INSERT_TOKEN, null, NotesContract.Notes.CONTENT_URI, cv);
        }
    }

    /** Creates a dialog asking for confirmation to delete the note */
    private void deleteNoteDialog() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
        deleteDialog.setMessage(R.string.delete_dialog)
                .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        mIsDeleted = true;
                        if (mNoteUri != null) {
                            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                                    getContentResolver());
                            mHandle.startDelete(NOTE_DELETE_TOKEN, null, mNoteUri, null, null);
                        }

                        if (mDualPane) {
                            NoteFragment noteFrag = new NoteFragment();

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.remove(noteFrag).commit();
                        } else {
                            getActivity().finish();
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // nothing
                    }
                });
        AlertDialog deleteAlert = deleteDialog.create();
        deleteAlert.show();

    }

    /** Handles content provider work asynchronously. The overridden methods here are
     *  for actually using the results of that work
     */
    private class NoteAsyncQueryHandler extends AsyncQueryHandler {
        public NoteAsyncQueryHandler (ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            if (c != null && c.moveToFirst()) {
                mEditTitle.setText(c.getString(c.getColumnIndex(NotesContract.Notes.COLUMN_TITLE)));
                mEditNote.setText(c.getString(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE)));

                long dateModified = c.getLong(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE_MODIFIED));
                mTextDateModified.setText(getString(R.string.last_modified) +
                        DateFormat.format(DATE_TIME_FORMAT, dateModified));
                mCurrentTag = c.getString(c.getColumnIndex(NotesContract.Tags.COLUMN_TAGS));
                if (mCurrentTag != null && mCurrentTag.length() > 0) {
                    mTagsBtn.setText(mCurrentTag);
                    mCurrentTagId = c.getLong(c.getColumnIndex(NotesContract.Tags.COLUMN_ID));
                }

                c.close();
                mEditTitle.addTextChangedListener(noteChangedListener);
                mEditNote.addTextChangedListener(noteChangedListener);
                if (!mDualPane) {
                    ((ActionBarActivity)getActivity()).getSupportActionBar()
                            .setTitle(mEditTitle.getText());
                }
            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            if (token == NOTE_INSERT_TOKEN) {
                mNoteUri = uri;
            }
            if (token == TAG_INSERT_TOKEN) {
                mCurrentTagId = Long.parseLong(uri.getLastPathSegment());
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // ???
        }
    }
}