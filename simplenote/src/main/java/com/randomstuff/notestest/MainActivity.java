package com.randomstuff.notestest;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity implements NoteListFragment.OnNoteSelectedListener,
        DrawerNavFragment.NavDrawerCallbacks {
    private boolean mDualPane;
    private DrawerNavFragment mDrawerNavFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the initial layout, currently based on either landscape or portrait
        setContentView(R.layout.main);

        // If the notes fragment is visible, we are in dual pane mode
        View notesFrame = findViewById(R.id.notes);
        mDualPane = (notesFrame != null) && (notesFrame.getVisibility() == View.VISIBLE);

        // Setting up the navigation drawer fragment
        mDrawerNavFragment = (DrawerNavFragment) getFragmentManager()
                .findFragmentById(R.id.drawer_frag);


        // Getting intent information to then deal with a share.
        Intent intent = getIntent();
        String action = intent.getAction();
        String shareType = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(shareType)) {
            startShare(intent);
        }

    }

    public void onNoteSelected(long id) {
        // fragment manager to change the note
        // id == -1 is for creating a new note
        if (id == -1L) {
            if (mDualPane) {
                NoteFragment noteFrag = (NoteFragment)
                        getFragmentManager().findFragmentById(R.id.notes);
                if (noteFrag == null || noteFrag.getShownId() > -2L) {
                    noteFrag = NoteFragment.newInstance(0L);
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.notes, noteFrag).commit();
                }
            } else {
                // this happens only in portrait mode
                Intent i = new Intent(this, NoteActivity.class);
                i.putExtra("id", 0L);
                startActivity(i);
            }
        } else {
            if (mDualPane) {
                NoteFragment noteFrag = (NoteFragment)
                        getFragmentManager().findFragmentById(R.id.notes);

                if (noteFrag == null || noteFrag.getShownId() != id) {
                    noteFrag = NoteFragment.newInstance(id);

                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.replace(R.id.notes, noteFrag).commit();
                }
            }
            else {
                Intent i = new Intent();
                i.setClass(this, NoteActivity.class);
                i.putExtra("id", id);
                startActivity(i);
            }
        }
    }

    public void onDrawerItemSelected(long id) {
        // deal with sending tag id to notelistfragment to refresh loader, or maybe can call it here
    }

    private void startShare(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            // do something, ie use code from notelistfragment new note
            //check for dual pane, use noteactivity or not based on that
            if (mDualPane) {
                NoteFragment noteFrag;
                noteFrag = NoteFragment.newInstance(-1L, sharedText);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.notes, noteFrag).commit();
            } else {
                Intent i=new Intent(this, NoteActivity.class);
                i.putExtra("id", -1L);
                i.putExtra("share", sharedText);
                startActivity(i);
            }
        }
    }
}
