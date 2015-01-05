package com.randomstuff.notestest;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;

/** The main activity for the app. Starts the navigation drawer, handles note selection through
 *  an interface, receives share data from other apps.
 */
public class MainActivity extends ActionBarActivity implements NoteListFragment.OnNoteSelectedListener,
        DrawerNavFragment.NavDrawerCallbacks {
    private boolean mDualPane;
    private DrawerNavFragment mDrawerNavFragment;
    private String mCurrentTag;
    private long mCurrentTagId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the initial layout, which will change with a larger screen to a dual pane layout
        setContentView(R.layout.activity_main);

        // The toolbar takes the place of a dedicated actionbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
        }

        // If the notes fragment is visible, we are in dual pane mode
        View notesFrame = findViewById(R.id.notes);
        mDualPane = (notesFrame != null) && (notesFrame.getVisibility() == View.VISIBLE);

        // Setting up the navigation drawer fragment
        mDrawerNavFragment = (DrawerNavFragment) getFragmentManager()
                .findFragmentById(R.id.drawer_left);

        mDrawerNavFragment.setUp(R.id.drawer_left,
                (DrawerLayout) findViewById(R.id.drawer_layout),
                mDualPane);

        // Creates the list of notes fragment, as that will always be displayed initially
        NoteListFragment nFrag = NoteListFragment.newInstance(-1L);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.notes_list, nFrag).commit();

        // Gets intent information to then deal with a share.
        Intent intent = getIntent();
        String action = intent.getAction();
        String shareType = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(shareType)) {
            startShare(intent);
        }

        // Creates a reference to the floating action button for creating a note
        ImageButton mCreateNote = (ImageButton) findViewById(R.id.create_note);
        final int[] mCreateNoteCoord = new int[2];
        mCreateNote.getLocationOnScreen(mCreateNoteCoord);
        // Sets an ontouchlistener to then find if the touch event was in the circular button
        mCreateNote.setOnTouchListener(new ImageButton.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent m) {
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    int dx = (int) m.getRawX() - mCreateNoteCoord[0];
                    int dy = (int) m.getRawY() - mCreateNoteCoord[1];
                    double d = Math.sqrt((dx * dx) + (dy * dy));
                    if (d < R.dimen.btn_floating_action_diameter) {
                        // create note!
                    }
                }
                return true;
            }
        });

    }
    /** Creates a note fragment and gives it the requested note's _id */
    @Override
    public void onNoteSelected(long id) {
        //This is to load an existing note

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

    /** Replaces the note list fragment with a new one, based on the selected tag */
    @Override
    public void onDrawerItemSelected(long id, String tag) {
        // Restarts the note list to show the chosen tag
        if (id > 0) {
            NoteListFragment listFrag = (NoteListFragment)getFragmentManager().findFragmentById(R.id.notes_list);
            if (id != mCurrentTagId && listFrag != null && listFrag.isVisible()) {
                mCurrentTag = tag;
                mCurrentTagId = id;
                if (mCurrentTag != null && mCurrentTag.length() > 0) {
                    getSupportActionBar().setTitle(mCurrentTag);

                }
                NoteListFragment nFrag = NoteListFragment.newInstance(mCurrentTagId);
                FragmentTransaction ft = getFragmentManager().beginTransaction();
                ft.replace(R.id.notes_list, nFrag).commit();
            }
        }
        // Starts the 'About' activity
        if (id == -2L) {
            Intent i = new Intent(this, SimpleDisplayActivity.class);
            i.putExtra("file", "about.txt");
            startActivity(i);
        }

    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        if (mCurrentTag != null && mCurrentTag.length() > 0) {
            actionBar.setTitle(mCurrentTag);
        } else {
            actionBar.setTitle(R.string.app_name);

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mDrawerNavFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case(R.id.add_note):
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

                return true;
            case(R.id.help):
                Intent i = new Intent(this, SimpleDisplayActivity.class);
                i.putExtra("file", "help.txt");
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /** Creates/replaces the note fragment when the app receives a share, and passes the shared text*/
    private void startShare(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText != null) {
            if (mDualPane) {
                NoteFragment noteFrag = NoteFragment.newInstance(-1L, sharedText);
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
