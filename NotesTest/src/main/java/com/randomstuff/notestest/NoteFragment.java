package com.randomstuff.notestest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
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
import android.widget.Toast;

public class NoteFragment extends Fragment implements TextWatcher {
    private EditText editTitle = null;
    private EditText editNote = null;
    private TextView textDateModified = null;

    private Intent shareIntent = new Intent().setAction(Intent.ACTION_SEND);

    private boolean isDeleted = false;
    private boolean isChanged = false;
    private boolean mDualPane;
    private Uri noteUri = null;
    private ShareActionProvider mShareActionProvider;

    static NoteFragment newInstance(long id) {
        NoteFragment frag = new NoteFragment();
        Log.d("id", String.valueOf(id));


        Bundle args = new Bundle();
        args.putLong("id", id);
        frag.setArguments(args);

        return (frag);
    }

    public long getShownId() {
        return getArguments().getLong("id", 0L);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        long mId = getArguments().getLong("id");
        if ( mId > 0) {
            noteUri = Uri.parse(Provider.Constants.CONTENT_URI + "/" + mId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.notes_detail, container, false);

        editTitle = (EditText) result.findViewById(R.id.edit_title);
        editNote = (EditText) result.findViewById(R.id.edit_note);
        textDateModified = (TextView) result.findViewById(R.id.text_date_modified);

        Log.d("id + uri", String.valueOf(getShownId()) + " " + noteUri);
        if (noteUri != null) {
            fillNote(noteUri);
        }

        //editTitle.addTextChangedListener(editTitleWatcher);
        editNote.addTextChangedListener(this);

        shareIntent.setType("text/plain");

        View notesListFrame = getActivity().findViewById(R.id.notes_list);
        mDualPane = (notesListFrame != null) && (notesListFrame.getVisibility() == View.VISIBLE);

        setHasOptionsMenu(true);

        if (!mDualPane) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);}
        return (result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notes, menu);

        mShareActionProvider = (ShareActionProvider) menu
                .findItem(R.id.share_button).getActionProvider();
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
                isDeleted = true;
                deleteNote();

                return true;
        }

        return (super.onOptionsItemSelected(item));
    }

    @Override
    public void onPause() {
        if (!isDeleted) {
            saveNote();
        }

        super.onPause();
    }

    @Override
    public void afterTextChanged(Editable s) {
        shareIntent.putExtra(Intent.EXTRA_TEXT, editNote.getText().toString());
        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(Intent.createChooser(shareIntent,
                    getResources().getString(R.string.share_choose)));
        }

        //add boolean for saving if text is changed
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


    private void fillNote(Uri uri) {
        //possibvly use asyncqueryhandler
        String[] projection = { Provider.Constants.COLUMN_ID,
                Provider.Constants.COLUMN_TITLE,
                Provider.Constants.COLUMN_NOTE,
                Provider.Constants.COLUMN_NOTE_MODIFIED };

        NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                getContentResolver());
        mHandle.startQuery(1, null, uri, projection, null, null, null);

        //Cursor c = getActivity().getContentResolver().query(uri, projection, null, null, null);
        // and all that stuff in query about if cursor != null etc
    }

    private void saveNote() {
        boolean titleEmpty = editTitle.getText().toString().isEmpty();
        boolean noteEmpty = editNote.getText().toString().isEmpty();

        if (!titleEmpty && !noteEmpty && noteUri != null && isChanged) {
            ContentValues cv = new ContentValues();

            cv.put(Provider.Constants.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE, editNote.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());

            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startUpdate(3, null, noteUri, cv, null, null);

            //getActivity().getContentResolver().update(noteUri, cv, null, null);
        } else if (!titleEmpty && !noteEmpty && noteUri == null) {
            ContentValues cv = new ContentValues();

            cv.put(Provider.Constants.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE, editNote.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());


            NoteAsyncQueryHandler mHandle = new NoteAsyncQueryHandler(getActivity().
                    getContentResolver());
            mHandle.startInsert(2, null, noteUri, cv);
            //noteUri = getActivity().getContentResolver().insert(Provider.Constants.CONTENT_URI, cv);
        } else {
            CharSequence saveFail = "Save failed. Must have a title and a note";
            Toast.makeText(getActivity(), saveFail, Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteNote() {
        //alertDIALOG!
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getActivity());
        deleteDialog.setMessage(R.string.delete_dialog)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
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
                        //getActivity().getContentResolver().delete(noteUri, null, null);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int position) {
                        if (mDualPane) {
                            NoteFragment noteFrag = new NoteFragment();

                            FragmentTransaction ft = getFragmentManager().beginTransaction();
                            ft.remove(noteFrag).commit();
                        } else {
                            getActivity().finish();
                        }
                    }
                });
        deleteDialog.show();

    }


    private class NoteAsyncQueryHandler extends AsyncQueryHandler {
        public NoteAsyncQueryHandler (ContentResolver cr) {
            super(cr);
        }

        @Override
        protected void onQueryComplete(int token, Object cookie, Cursor c) {
            if (c != null && c.moveToFirst()) {
                editTitle.setText(c.getString(c.getColumnIndex(Provider.Constants.COLUMN_TITLE)));
                editNote.setText(c.getString(c.getColumnIndex(Provider.Constants.COLUMN_NOTE)));

                long dateModified = c.getLong(c.getColumnIndex(Provider.Constants.COLUMN_NOTE_MODIFIED));
                Log.d("dateMOdified", Long.toString(dateModified));
                textDateModified.setText("Last Modified " +
                        DateFormat.format("h:m a, LLL d", dateModified));

                c.close();
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