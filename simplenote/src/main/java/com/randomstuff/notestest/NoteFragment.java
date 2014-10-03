package com.randomstuff.notestest;

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
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

public class NoteFragment extends Fragment implements TagDialogFragment.TagDialogCallbacks {
    private EditText editTitle = null;
    private EditText editNote = null;
    private TextView textDateModified = null;
    private TextWatcher noteChangedListener;
    private Button tagsBtn;

    private long currentNoteId;
    private String currentTag;
    private long currentTagId = 0L;
    private boolean isDeleted = false;
    private boolean isChanged = false;

    private boolean mDualPane;
    private Uri noteUri = null;

    private static final int NOTE_INSERT_TOKEN = 1;
    private static final int NOTE_UPDATE_TOKEN = 2;
    private static final int NOTE_QUERY_TOKEN = 3;
    private static final int NOTE_DELETE_TOKEN = 4;
    private static final int TAG_INSERT_TOKEN = 5;
    private static final int NOTE_TAG_DELETE_TOKEN = 6;

    public static NoteFragment newInstance(long id) {
        NoteFragment frag = new NoteFragment();
        Log.d("id", String.valueOf(id));


        Bundle args = new Bundle();
        args.putLong("id", id);
        frag.setArguments(args);

        return frag;
    }

    public static NoteFragment newInstance(long id, String shareText) {
        NoteFragment frag = new NoteFragment();
        Log.d("id", String.valueOf(id));


        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putString("share", shareText);
        frag.setArguments(args);

        return frag;
    }

    public long getShownId() {
        return getArguments().getLong("id", 0L);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        currentNoteId = getShownId();
        if ( currentNoteId > 0L) {
            noteUri = Uri.parse(NotesContract.Notes.CONTENT_URI + "/" + currentNoteId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.notes_detail, container, false);

        View notesListFrame = getActivity().findViewById(R.id.notes_list);
        mDualPane = (notesListFrame != null) && (notesListFrame.getVisibility() == View.VISIBLE);

        editTitle = (EditText) result.findViewById(R.id.edit_title);
        editNote = (EditText) result.findViewById(R.id.edit_note);
        textDateModified = (TextView) result.findViewById(R.id.text_date_modified);
        tagsBtn = (Button) result.findViewById(R.id.choose_tag_btn);


        if (noteUri != null) {
            fillNote(noteUri);
        } else if (currentNoteId == -1L) {
            editNote.setText(getArguments().getString("share"));
            noteWatcher();
        } else {
            noteWatcher();
        }

        setHasOptionsMenu(true);

        tagsBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                FragmentManager fm = getFragmentManager();
                TagDialogFragment frag = TagDialogFragment.newInstance(currentTagId);
                frag.setTargetFragment(NoteFragment.this, 0);
                frag.show(fm, "add_tag");
            }
        });

        if (!mDualPane) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        return (result);
    }

    @Override
    public void onTagChosen(String tag, long id) {
        // If id is -1, that means the user entered a new tag
        if (id == -1L) {
            isChanged = true;
            currentTag = tag;
            tagsBtn.setText(currentTag);

            ContentValues cv = new ContentValues();
            cv.put(NotesContract.Tags.COLUMN_TAGS, tag);
            NoteAsyncQueryHandler mHandle =
                    new NoteAsyncQueryHandler(getActivity().getContentResolver());
            mHandle.startInsert(TAG_INSERT_TOKEN, null, NotesContract.Tags.CONTENT_URI, cv);

        }
        // If the id is more than 0, the tag already exists. Tagid will be saved on note save
        if (id > 0L) {
            isChanged = true;
            currentTag = tag;
            currentTagId = id;
            tagsBtn.setText(currentTag);
        }
        // If -2 is passed as the id, it means to remove the tag from the current note
        if (id == -2L && currentTag.length() > 0) {
            NoteAsyncQueryHandler mHandle =
                    new NoteAsyncQueryHandler(getActivity().getContentResolver());
            mHandle.startDelete(
                    NOTE_TAG_DELETE_TOKEN,
                    null,
                    NotesContract.Tags_Notes.CONTENT_URI,
                    null,
                    new String[]{ Long.toString(currentNoteId)});
            isChanged = true;
            currentTag = null;
            currentTagId = 0L;
            tagsBtn.setText(R.string.choose_tag);
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
        editNote.removeTextChangedListener(noteChangedListener);
        if (!isDeleted) {
            saveNote();
        }

        super.onPause();
    }


    public void setActionShareIntent() {
        Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_TEXT, editNote.getText().toString());
        startActivity(Intent.createChooser(shareIntent,
                getResources().getString(R.string.share_choose)));

    }

    public void noteWatcher() {
        noteChangedListener = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                isChanged = true;
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
        editNote.addTextChangedListener(noteChangedListener);
    }

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

    private void saveNote() {
        boolean titleEmpty = editTitle.getText().toString().isEmpty();
        boolean noteEmpty = editNote.getText().toString().isEmpty();

        if ((!titleEmpty || !noteEmpty) && noteUri != null && isChanged) {
            ContentValues cv = new ContentValues();
            cv.put(NotesContract.Notes.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE, editNote.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());
            cv.put(NotesContract.Tags_Notes.COLUMN_TAGS_ID, currentTagId);

            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startUpdate(NOTE_UPDATE_TOKEN, null, noteUri, cv, null, null);
        }
        if ((!titleEmpty || !noteEmpty) && noteUri == null) {
            ContentValues cv = new ContentValues();
            cv.put(NotesContract.Notes.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE, editNote.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());
            cv.put(NotesContract.Tags_Notes.COLUMN_TAGS_ID, currentTagId);

            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startInsert(NOTE_INSERT_TOKEN, null, NotesContract.Notes.CONTENT_URI, cv);
        }
    }

    private void deleteNoteDialog() {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
        deleteDialog.setMessage(R.string.delete_dialog)
                .setPositiveButton(R.string.delete_button, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        isDeleted = true;
                        if (noteUri != null) {
                            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                                    getContentResolver());
                            mHandle.startDelete(NOTE_DELETE_TOKEN, null, noteUri, null, null);
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


    private class NoteAsyncQueryHandler extends AsyncQueryHandler {
        public NoteAsyncQueryHandler (ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            if (c != null && c.moveToFirst()) {
                editTitle.setText(c.getString(c.getColumnIndex(NotesContract.Notes.COLUMN_TITLE)));
                editNote.setText(c.getString(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE)));

                long dateModified = c.getLong(c.getColumnIndex(NotesContract.Notes.COLUMN_NOTE_MODIFIED));
                Log.d("dateMOdified", Long.toString(dateModified));
                textDateModified.setText(getString(R.string.last_modified) +
                        DateFormat.format("h:mm a, LLL d", dateModified));
                currentTag = c.getString(c.getColumnIndex(NotesContract.Tags.COLUMN_TAGS));
                if (currentTag.length() > 0) {
                    tagsBtn.setText(currentTag);
                    currentTagId = c.getLong(c.getColumnIndex(NotesContract.Tags.COLUMN_ID));
                }

                c.close();
                noteWatcher();
                if (!mDualPane) {
                    getActivity().getActionBar().setTitle(editTitle.getText());
                }
            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            if (token == NOTE_INSERT_TOKEN) {
                noteUri = uri;
            }
            if (token == TAG_INSERT_TOKEN) {
                currentTagId = Long.parseLong(uri.getLastPathSegment());
            }
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // ???
        }
    }
}