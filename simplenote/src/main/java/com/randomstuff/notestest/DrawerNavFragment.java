package com.randomstuff.notestest;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

/** Creates and manages a navigation drawer using a fragment
 *
 */
public class DrawerNavFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, TagCursorAdapter.OnTagDeleteListener {
    // Remembers the currently selected position
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    private static final String STATE_SELECTED_INDEX = "selected_navigation_drawer_index";
    //this stores whether the user has seen the navigation drawer opened in shared preferences
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    // pointer to current callbacks instance
    private NavDrawerCallbacks mCallbacks;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;
    private Toolbar mToolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    private int mCurrentSelectedPosition = -1;
    private long mCurrentSelectedIndex = -1L;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private TagCursorAdapter mAdapter = null;
    private static final int LOADER_ID = 1;
    private static final int HANDLER_DELETE_TAG_ID = 2;
    private static final int HANDLER_DELETE_TAGGED_NOTES = 3;
    private static final int HANDLER_DELETE_TAGGED_ROWS = 4;

    public DrawerNavFragment() {
    }

    /** Sends the creating activity information on the selected tag */
    public interface NavDrawerCallbacks {
        void onDrawerItemSelected(long id, String tag);
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Checks if the calling activity implements the interface for tag information
        try {
            mCallbacks = (NavDrawerCallbacks) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() +
                    " must implement NavDrawerCallbacks");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallbacks = null;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Finds the calling activity's toolbar, used as an actionbar
        mToolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);
        // Checks if the activity has been started before, to open the drawer if not in setUp
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mCurrentSelectedIndex = savedInstanceState.getLong(STATE_SELECTED_INDEX);
            mFromSavedInstanceState = true;
        }

        selectItem(mCurrentSelectedPosition, mCurrentSelectedIndex, null);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View result = inflater.inflate(R.layout.drawer_frag, container, false);

        mDrawerListView = (ListView) result.findViewById(R.id.drawer_list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mDrawerListView.getHeaderViewsCount() == 1 && position == 0) {
                    // The All notes header
                    selectItem(-1, -1L, null);
                } else if (position == mDrawerListView.getCount() - 1){
                    // About choice
                    // TODO pass some things to get the mainactivity to show the about page
                    // TODO find an About icon for the footer about choice
                    Toast.makeText(getActivity(), "You chose a header or footer", Toast.LENGTH_SHORT).show();
                } else {
                    Cursor c = (Cursor) mDrawerListView.getItemAtPosition(position);
                    String mSelectedTag =
                            c.getString(c.getColumnIndex(NotesContract.Tags.COLUMN_TAGS));
                    selectItem(position, id, mSelectedTag);
                }
            }
        });
        View header = inflater.inflate(R.layout.header_all_notes, null);
        View footerAbout = inflater.inflate(R.layout.footer_about, null);
        mDrawerListView.addHeaderView(header);
        mDrawerListView.addFooterView(footerAbout);
        // Sets current listview to the custom cursor adapter
        mAdapter = new TagCursorAdapter(getActivity(),
                null,
                0,
                this);

        mDrawerListView.setAdapter(mAdapter);
        // Begins cursorloader
        getLoaderManager().initLoader(LOADER_ID, null, this);
        if (mCurrentSelectedPosition > 0) {
            // TODO Probably need to change this to use the selected id, as position could change
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        }
        return result;
    }

    /** Sets the responses to various navigation drawer events */
    public void setUp(int fragmentId, DrawerLayout drawerLayout, boolean mDualPane) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        if (!mDualPane) {
            ActionBar actionBar = getActionBar();
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
        }

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout,
                mToolbar, R.string.nav_drawer_open, R.string.nav_drawer_close) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                if (!isAdded()) {
                    return;
                }
                getActivity().invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                if (!isAdded()) {
                    return;
                }

                if (!mUserLearnedDrawer) {
                    mUserLearnedDrawer = true;
                    SharedPreferences sp = PreferenceManager
                            .getDefaultSharedPreferences(getActivity());
                    sp.edit().putBoolean(PREF_USER_LEARNED_DRAWER, true).apply();
                }
                getActivity().invalidateOptionsMenu();
            }
        };

        if (!mUserLearnedDrawer && !mFromSavedInstanceState) {
            mDrawerLayout.openDrawer(mFragmentContainerView);
        }

        mDrawerLayout.post(new Runnable() {
            @Override
            public void run() {
                mDrawerToggle.syncState();
            }
        });

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_SELECTED_POSITION, mCurrentSelectedPosition);
        outState.putLong(STATE_SELECTED_INDEX, mCurrentSelectedIndex);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (mDrawerLayout != null && isDrawerOpen()) {
            inflater.inflate(R.menu.global, menu);
            showGlobalContextActionBar();
        }
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.delete_tag:
                // create a dialog to add a new tag
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ActionBar getActionBar() {
        return ((ActionBarActivity)getActivity()).getSupportActionBar();
    }

    private void showGlobalContextActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(R.string.app_name);
    }

    /** Deletes tags, and possibly notes, based on the deleteNotes parameter */
    @Override
    public void onDeleteTag(int position, int deleteNotes) {
        // Delete the tag, and all the notes with that tag
        if (deleteNotes == 1) {
            TagAsyncQueryHandler mHandle =
                    new TagAsyncQueryHandler(getActivity().getContentResolver());
            String tagId = Long.toString(mDrawerListView.getItemIdAtPosition(position));
            String[] selectionArgs = new String[]{ tagId };
            mHandle.startDelete(HANDLER_DELETE_TAG_ID,
                    null,
                    NotesContract.Tags.CONTENT_URI,
                    null,
                    selectionArgs);
            // delete the tagged notes from notes table. A trigger on note delete removes the tags_notes row
            mHandle.startDelete(HANDLER_DELETE_TAGGED_NOTES,
                    null,
                    NotesContract.Notes.TAGGED_NOTES,
                    null,
                    selectionArgs);
        // Delete the tag, and the references to notes with that tag in tags_notes table
        } else {
            TagAsyncQueryHandler mHandle =
                    new TagAsyncQueryHandler(getActivity().getContentResolver());
            String tagId = Long.toString(mDrawerListView.getItemIdAtPosition(position));
            String[] selectionArgs = new String[]{ tagId };
            // delete the tag from tags table
            mHandle.startDelete(HANDLER_DELETE_TAG_ID,
                    null,
                    NotesContract.Tags.CONTENT_URI,
                    null,
                    selectionArgs);
            //deletes the rows in tag_notes with the deleted tag
            mHandle.startDelete(HANDLER_DELETE_TAGGED_ROWS,
                    null,
                    NotesContract.Tags_Notes.CONTENT_URI,
                    null,
                    selectionArgs);

        }
    }

    /** Creates an asynchronous task to handle database work, specifically deletes */
    private class TagAsyncQueryHandler extends AsyncQueryHandler {
        public TagAsyncQueryHandler(ContentResolver cr) {
            super(cr);
        }

        @Override
        public void onDeleteComplete(int token, Object cookie, int result) {

        }
    }

    public boolean isDrawerOpen() {
        return mDrawerLayout != null && mDrawerLayout.isDrawerOpen(mFragmentContainerView);
    }

    /** Informs the calling activity which tag is selected, and closes the drawer */
    private void selectItem(int position, long id, String tag) {
        mCurrentSelectedPosition = position;
        mCurrentSelectedIndex = id;
        if (mDrawerListView != null && position != -1) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onDrawerItemSelected(id, tag);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Uri baseUri = NotesContract.Tags.CONTENT_URI;
        String[] projection = new String[] {
                NotesContract.Tags.COLUMN_ID,
                NotesContract.Tags.COLUMN_TAGS };

        return new CursorLoader(getActivity(), baseUri,
                projection, null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
