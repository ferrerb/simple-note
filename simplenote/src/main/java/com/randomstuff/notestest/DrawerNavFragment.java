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
    private Toolbar toolbar;

    private ActionBarDrawerToggle mDrawerToggle;

    private int mCurrentSelectedPosition = -1;
    private long mCurrentSelectedIndex = -1L;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    private TagCursorAdapter adapter = null;
    private static final int LOADER_ID = 1;

    public DrawerNavFragment() {

    }

    public interface NavDrawerCallbacks {
        void onDrawerItemSelected(long id, String tag);
    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

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

        toolbar = (Toolbar)getActivity().findViewById(R.id.toolbar);

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
        final Button notesBtn = (Button) result.findViewById(R.id.all_notes_btn);
        notesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectItem(-1, -1L, null);
            }
        });

        mDrawerListView = (ListView) result.findViewById(R.id.drawer_list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) mDrawerListView.getItemAtPosition(position);
                String mSelectedTag = c.getString(c.getColumnIndex(NotesContract.Tags.COLUMN_TAGS));
                Log.d("selected tag from navigation drawer", mSelectedTag);
                selectItem(position, id, mSelectedTag);
            }
        });
        // Sets current listview to the cursoradapter
        adapter = new TagCursorAdapter(getActivity(),
                null,
                0,
                this);

        mDrawerListView.setAdapter(adapter);
        // Begins cursorloader
        getLoaderManager().initLoader(LOADER_ID, null, this);
        if (mCurrentSelectedPosition > 0) {
            // TODO Probably need to change this to use the selected id, as position could change
            mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
        }
        return result;
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout) {
        mFragmentContainerView = getActivity().findViewById(fragmentId);
        mDrawerLayout = drawerLayout;

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), mDrawerLayout,
                toolbar, R.string.nav_drawer_open, R.string.nav_drawer_close) {
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

    @Override
    public void onDeleteTag(long id, int deleteNotes) {
        if (deleteNotes == 1) {
            // delete tag and notes tagged
            TagAsyncQueryHandler mHandle =
                    new TagAsyncQueryHandler(getActivity().getContentResolver());
        } else {
            // delete just tag, and delete the notes in tags_notes with that tag
            TagAsyncQueryHandler mHandle =
                    new TagAsyncQueryHandler(getActivity().getContentResolver());

        }
    }

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
    //This handles both selections in the listview, and hitting the All notes button
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
        adapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }
}
