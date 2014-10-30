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
import android.view.View;

public class MainActivity extends ActionBarActivity implements NoteListFragment.OnNoteSelectedListener,
        DrawerNavFragment.NavDrawerCallbacks {
    private boolean mDualPane;
    private DrawerNavFragment mDrawerNavFragment;
    private String mCurrentTag;
    private long mCurrentTagId = -1L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the initial layout, currently based on either landscape or portrait
        setContentView(R.layout.activity_main);

        // using the new toolbar instead of the actionbar
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
                (DrawerLayout) findViewById(R.id.drawer_layout));

        NoteListFragment nFrag = NoteListFragment.newInstance(-1L);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.notes_list, nFrag).commit();

        // Getting intent information to then deal with a share.
        Intent intent = getIntent();
        String action = intent.getAction();
        String shareType = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(shareType)) {
            startShare(intent);
        }

    }

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

    @Override
    public void onDrawerItemSelected(long id, String tag) {
        // do something here to check if the tag is changed, and see about fragment visibility
        NoteListFragment listFrag = (NoteListFragment)getFragmentManager().findFragmentById(R.id.notes_list);
        if (id != mCurrentTagId && listFrag != null && listFrag.isVisible()) {
            mCurrentTag = tag;
            mCurrentTagId = id;
            if (mCurrentTag != null && mCurrentTag.length() > 0) {
                getSupportActionBar().setTitle(mCurrentTag);

            }
            Log.d("tag id passed to mainactivity from drawer", Long.toString(mCurrentTagId));
            NoteListFragment nFrag = NoteListFragment.newInstance(mCurrentTagId);
            FragmentTransaction ft = getFragmentManager().beginTransaction();
            ft.replace(R.id.notes_list, nFrag).commit();
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
            case(R.id.settings):
                return true;
            case(R.id.help):
                Intent i = new Intent(this, SimpleDisplayActivity.class);
                i.putExtra("file", "help.txt");
                startActivity(i);
                return true;
            case(R.id.about):
                i = new Intent(this, SimpleDisplayActivity.class);
                i.putExtra("file", "about.txt");
                startActivity(i);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

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
