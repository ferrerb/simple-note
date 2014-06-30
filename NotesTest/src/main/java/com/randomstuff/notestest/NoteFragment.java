package com.randomstuff.notestest;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

public class NoteFragment extends Fragment {
    private EditText editTitle = null;
    private EditText editNote = null;
    private boolean isDeleted = false;
    private boolean mDualPane;

    public Uri noteUri = null;

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
        setHasOptionsMenu(true);

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

        Log.d("index + uri", String.valueOf(getShownIndex()) + " " + noteUri);
        if (noteUri != null) {
            fillNote(noteUri);
        }

        View notesListFrame = getActivity().findViewById(R.id.notes_list);
        mDualPane = (notesListFrame != null) && (notesListFrame.getVisibility() == View.VISIBLE);

        return (result);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.notes, menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //maybe add confirm button in portrait mode
        if (item.getItemId() == R.id.delete) {
            if (noteUri != null) {
                getActivity().getContentResolver().delete(noteUri, null, null);
            }
            isDeleted = true;
            //call provider delete

            if (mDualPane) {
                NoteFragment noteFrag = new NoteFragment();

                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.remove(noteFrag).commit();
            } else {
                getActivity().finish();
            }

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

    private void fillNote(Uri uri) {
        //possibvly use asyncqueryhandler
        String[] projection = { Provider.Constants.COLUMN_ID,
                Provider.Constants.COLUMN_TITLE,
                Provider.Constants.COLUMN_NOTE };

        Cursor c = getActivity().getContentResolver().query(uri, projection, null, null, null);

        if (c != null && c.moveToFirst()) {
            editTitle.setText(c.getString(c.getColumnIndex(Provider.Constants.COLUMN_TITLE)));
            editNote.setText(c.getString(c.getColumnIndex(Provider.Constants.COLUMN_NOTE)));

            c.close();
        }
    }

    private void saveNote() {
        boolean titleEmpty = editTitle.getText().toString().isEmpty();
        boolean noteEmpty = editNote.getText().toString().isEmpty();

        if (!titleEmpty && !noteEmpty && noteUri != null) {
            ContentValues cv = new ContentValues();

            cv.put("title", editTitle.getText().toString());
            cv.put("note", editNote.getText().toString());

            getActivity().getContentResolver().update(noteUri, cv, null, null);
        } else if (!titleEmpty && !noteEmpty && noteUri == null) {
            ContentValues cv = new ContentValues();

            cv.put("title", editTitle.getText().toString());
            cv.put("note", editNote.getText().toString());

            noteUri = getActivity().getContentResolver().insert(Provider.Constants.CONTENT_URI, cv);
        } else {
            CharSequence saveFail = "Save failed. Must have a title and a note";
            Toast.makeText(getActivity(), saveFail, Toast.LENGTH_SHORT).show();
        }


    }

}