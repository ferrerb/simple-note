package com.randomstuff.notestest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class MainActivity extends Activity implements NoteListFragment.OnNoteSelectedListener,
        DrawerNavFragment.NavDrawerCallbacks {
    private boolean mDualPane;
    private DrawerNavFragment mDrawerNavFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Sets the initial layout, currently based on either landscape or portrait
        setContentView(R.layout.activity_main);

        // If the notes fragment is visible, we are in dual pane mode
        View notesFrame = findViewById(R.id.notes);
        mDualPane = (notesFrame != null) && (notesFrame.getVisibility() == View.VISIBLE);

        // Setting up the navigation drawer fragment
        mDrawerNavFragment = (DrawerNavFragment) getFragmentManager()
                .findFragmentById(R.id.drawer_left);
        // TODO mtitle to get the tag name, and use it as the acion bar title

        mDrawerNavFragment.setUp(R.id.drawer_left,
                (DrawerLayout) findViewById(R.id.drawer_layout));


        // Getting intent information to then deal with a share.
        Intent intent = getIntent();
        String action = intent.getAction();
        String shareType = intent.getType();

        if (Intent.ACTION_SEND.equals(action) && "text/plain".equals(shareType)) {
            startShare(intent);
        }

    }

    public void onNoteSelected(long id) {
        // TODO clean this up, should only need one for new or load note
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

    @Override
    public void onDrawerItemSelected(long id) {
        NoteListFragment nFrag = NoteListFragment.newInstance(id);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.notes_list, nFrag).commit();
    }

    public void restoreActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        //TODO Set this to the tag name, maybe have callback from listfragment after it gets the data
        actionBar.setTitle(R.string.app_name);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mDrawerNavFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.options, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //TODO should i have the options.xml options here instead of in notelistfragment, search is issue
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
