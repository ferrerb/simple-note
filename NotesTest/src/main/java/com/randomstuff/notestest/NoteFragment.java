package com.randomstuff.notestest;

import android.app.AlertDialog;
import android.app.Fragment;
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
import android.widget.EditText;
import android.widget.ShareActionProvider;
import android.widget.TextView;

public class NoteFragment extends Fragment{
    private EditText editTitle = null;
    private EditText editNote = null;
    private TextView textDateModified = null;
    private TextWatcher noteChangedListener;

    private Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND);

    private boolean isDeleted = false;
    private boolean isChanged = false;

    private boolean mDualPane;
    private Uri noteUri = null;
    private ShareActionProvider mShareActionProvider;

    public static NoteFragment newInstance(long id) {
        NoteFragment frag = new NoteFragment();
        Log.d("id", String.valueOf(id));


        Bundle args = new Bundle();
        args.putLong("id", id);
        frag.setArguments(args);

        return frag;
    }

    public long getShownId() {
        return getArguments().getLong("id", 0L);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long mId = getArguments().getLong("id");
        if ( mId > 0) {
            noteUri = Uri.parse(NotesContract.Notes.CONTENT_URI + "/" + mId);
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

        Log.d("id + uri", String.valueOf(getShownId()) + " " + noteUri);
        if (noteUri != null) {
            fillNote(noteUri);
        } else {
            noteWatcher();
        }

        shareIntent.setType("text/plain");
        setHasOptionsMenu(true);

        if (!mDualPane) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        return (result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notes, menu);
        //TODO get share to work consistently on initial load of note, maybe just write own intent
        mShareActionProvider = (ShareActionProvider) menu
                .findItem(R.id.share_button).getActionProvider();
        mShareActionProvider.setShareIntent(setDefaultIntent());


    }

    public Intent setDefaultIntent() {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        return i;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //maybe add confirm button in portrait mode
        switch (item.getItemId()){
            //Should only be enabled if in portrait mode
            case (android.R.id.home):
                Intent i = new Intent(getActivity(), NotesTest.class);
                i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(i);
                getActivity().finish();
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

    public void noteWatcher() {
        noteChangedListener = new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
                Log.d("asdf", "why ist his called ");
                isChanged = true;

                shareIntent.putExtra(Intent.EXTRA_TEXT, editNote.getText().toString());
                if (mShareActionProvider != null) {
                    mShareActionProvider.setShareIntent(Intent.createChooser(shareIntent,
                            getResources().getString(R.string.share_choose)));
                }
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
        String[] projection = { NotesContract.Notes.COLUMN_ID,
                NotesContract.Notes.COLUMN_TITLE,
                NotesContract.Notes.COLUMN_NOTE,
                NotesContract.Notes.COLUMN_NOTE_MODIFIED };

        NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                getContentResolver());
        mHandle.startQuery(1, null, uri, projection, null, null, null);
    }

    private void saveNote() {
        boolean titleEmpty = editTitle.getText().toString().isEmpty();
        boolean noteEmpty = editNote.getText().toString().isEmpty();

        Log.d("isCHanged = ", Boolean.toString(isChanged));

        if (!titleEmpty && !noteEmpty && noteUri != null && isChanged) {
            ContentValues cv = new ContentValues();

            cv.put(NotesContract.Notes.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE, editNote.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());

            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startUpdate(3, null, noteUri, cv, null, null);
        }
        if (!titleEmpty && !noteEmpty && noteUri == null) {
            ContentValues cv = new ContentValues();

            cv.put(NotesContract.Notes.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE, editNote.getText().toString());
            cv.put(NotesContract.Notes.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());


            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startInsert(2, null, NotesContract.Notes.CONTENT_URI, cv);
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
                            mHandle.startDelete(4, null, noteUri, null, null);
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

                c.close();
                noteWatcher();
            }

            shareIntent.putExtra(Intent.EXTRA_TEXT, editNote.getText().toString());
            if (mShareActionProvider != null) {
                mShareActionProvider.setShareIntent(Intent.createChooser(shareIntent,
                        getResources().getString(R.string.share_choose)));
            }
        }

        @Override
        protected void onInsertComplete(int token, Object cookie, Uri uri) {
            noteUri = uri;
        }

        @Override
        protected void onDeleteComplete(int token, Object cookie, int result) {
            // ???
        }
    }
}