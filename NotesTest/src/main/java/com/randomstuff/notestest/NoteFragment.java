package com.randomstuff.notestest;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
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

public class NoteFragment extends Fragment {
    private EditText editTitle = null;
    private EditText editNote = null;
    private TextView textDateModified = null;
    private TextWatcher editTitleWatcher = null;
    private TextWatcher editNoteWatcher = null;

    private boolean isDeleted = false;
    private boolean mDualPane;
    private Uri noteUri = null;
    private ShareActionProvider mShareActionProvider;

    static NoteFragment newInstance(long id, int index) {
        NoteFragment frag = new NoteFragment();
        Log.d("id + index", String.valueOf(id) + " " + String.valueOf(index));


        Bundle args = new Bundle();
        args.putLong("id", id);
        args.putInt("index", index);
        frag.setArguments(args);

        return (frag);
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
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

        Log.d("index + uri", String.valueOf(getShownIndex()) + " " + noteUri);
        if (noteUri != null) {
            fillNote(noteUri);
        }

        //editTitle.addTextChangedListener(editTitleWatcher);
        //editNote.addTextChangedListener(editNoteWatcher);

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
        doShare();
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
                Log.d("noteuri during delete", " + " + noteUri);
                if (noteUri != null) {
                    getActivity().getContentResolver().delete(noteUri, null, null);
                }
                isDeleted = true;

                if (mDualPane) {
                    NoteFragment noteFrag = new NoteFragment();

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.remove(noteFrag).commit();
                } else {
                    getActivity().finish();
                }
                return true;
        }

        return (super.onOptionsItemSelected(item));
    }

    private void doShare () {
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.putExtra(Intent.EXTRA_TEXT, editNote.getText().toString());
        shareIntent.setType("text/plain");

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(Intent.createChooser(shareIntent,
                    getResources().getString(R.string.share_choose)));

            //mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    public void onPause() {
        if (!isDeleted) {
            saveNote();
        }

        super.onPause();
    }

    private void fillNote(Uri uri) {
        //possibvly use asyncqueryhandler
        String[] projection = { Provider.Constants.COLUMN_ID,
                Provider.Constants.COLUMN_TITLE,
                Provider.Constants.COLUMN_NOTE,
                Provider.Constants.COLUMN_NOTE_MODIFIED };

        Cursor c = getActivity().getContentResolver().query(uri, projection, null, null, null);

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

    private void saveNote() {
        boolean titleEmpty = editTitle.getText().toString().isEmpty();
        boolean noteEmpty = editNote.getText().toString().isEmpty();

        if (!titleEmpty && !noteEmpty && noteUri != null) {
            ContentValues cv = new ContentValues();

            cv.put(Provider.Constants.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE, editNote.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());

            getActivity().getContentResolver().update(noteUri, cv, null, null);
        } else if (!titleEmpty && !noteEmpty && noteUri == null) {
            ContentValues cv = new ContentValues();

            cv.put(Provider.Constants.COLUMN_TITLE, editTitle.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE, editNote.getText().toString());
            cv.put(Provider.Constants.COLUMN_NOTE_MODIFIED, System.currentTimeMillis());

            noteUri = getActivity().getContentResolver().insert(Provider.Constants.CONTENT_URI, cv);
        } else {
            CharSequence saveFail = "Save failed. Must have a title and a note";
            Toast.makeText(getActivity(), saveFail, Toast.LENGTH_SHORT).show();
        }


    }

}