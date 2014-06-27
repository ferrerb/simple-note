package com.randomstuff.notestest;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.widget.EditText;
import android.widget.Toast;

public class NoteFragment extends Fragment {
    private EditText editTitle = null;
    private EditText editNote = null;
    private boolean isDeleted = false;
    private boolean mDualPane;

    static NoteFragment newInstance(long id, int index) {
        NoteFragment frag = new NoteFragment();

        Uri noteUri;
        if (id > 0) {
            noteUri = Uri.parse(Provider.Constants.CONTENT_URI + "/" + id);
        }
        else {
            noteUri = null;
        }

        Bundle args = new Bundle();
        args.putInt("index", index);
        args.putParcelable("noteUri", noteUri);
        frag.setArguments(args);

        return (frag);
    }

    public Uri getShownUri() {
        return getArguments().getParcelable("noteUri");
    }

    public int getShownIndex() {
        return getArguments().getInt("index", 0);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View result = inflater.inflate(R.layout.notes_detail, container, false);

        editTitle = (EditText) result.findViewById(R.id.edit_title);
        editNote = (EditText) result.findViewById(R.id.edit_note);

        //if logic about -1 making a new note, otherwise getnoteasync
        if (getShownUri() != null) {
            fillNote(getShownUri());
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
        //deal with delete
        if (item.getItemId() == R.id.delete) {
            //call delete note and maybe move to another note
            //must do something diff for portrait and land
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
        //check if the edittexts are empty, dont save, or something
        boolean titleEmpty = editTitle.getText().toString().isEmpty();
        boolean noteEmpty = editNote.getText().toString().isEmpty();

        if (!isDeleted && !titleEmpty && !noteEmpty && getShownUri() != null) {
            //call provider insert
        } else if (!isDeleted && !titleEmpty && !noteEmpty && getShownUri() == null) {
            //call provider update
        } else {
            CharSequence saveFail = "Save failed. Must have a title and a note";
            Toast.makeText(getActivity(), saveFail, Toast.LENGTH_SHORT).show();
        }

        super.onPause();
    }

    private void fillNote(Uri uri) {
        //possibvly use asyncqueryhandler
        String[] projection = { };

        Cursor c = getActivity().getContentResolver().query(uri, projection,null, null, null);

        if (c != null && c.moveToFirst()) {
            editTitle.setText(c.getString(c.getColumnIndex(Provider.Constants.COLUMN_TITLE)));
            editNote.setText(c.getString(c.getColumnIndex(Provider.Constants.COLUMN_NOTE)));

            c.close();
        }
    }

    private void saveNote() {

    }

    private void createNote() {

    }
}