package com.randomstuff.notestest;

import android.app.ActionBar;
import android.app.Activity;
import android.app.Fragment;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

public class DrawerNavFragment extends Fragment {
    // Remembers the currently selected position
    private static final String STATE_SELECTED_POSITION = "selected_navigation_drawer_position";
    //this stores whether the user has seen the navigation drawer opened in shared preferences
    private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
    // pointer to current callbacks instance
    private NavDrawerCallbacks mCallbacks;

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private View mFragmentContainerView;

    private ActionBarDrawerToggle mDrawerToggle;

    private int mCurrentSelectedPosition = 0;
    private long mCurrentSelectedIndex = -1L;
    private boolean mFromSavedInstanceState;
    private boolean mUserLearnedDrawer;

    public DrawerNavFragment() {

    }

    public interface NavDrawerCallbacks {
        public void onDrawerItemSelected(long id);
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

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);

        if (savedInstanceState != null) {
            mCurrentSelectedPosition = savedInstanceState.getInt(STATE_SELECTED_POSITION);
            mFromSavedInstanceState = true;
        }

        selectItem(mCurrentSelectedPosition, mCurrentSelectedIndex);

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View result = inflater.inflate(R.layout.drawer_frag, container, false);
        final Button notesBtn = (Button) result.findViewById(R.id.all_notes_btn);
        notesBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                selectItem(-1, -1L);
            }
        });

        mDrawerListView = (ListView) result.findViewById(R.id.drawer_list);
        mDrawerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectItem(position, id);
            }
        });
        // create a cursorloader here! to setadapter
        ListAdapter adapter = new SimpleCursorAdapter(getActivity(), android.R.layout.simple_list_item_1, null, null, null, 0);
        mDrawerListView.setItemChecked(mCurrentSelectedPosition, true);
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
                R.drawable.ic_drawer, R.string.nav_drawer_open, R.string.nav_drawer_close) {

        };
    }

    private ActionBar getActionBar() {
        return getActivity().getActionBar();
    }

    //This handles both selections in the listview, and hitting the All notes button
    private void selectItem(int position, long id) {
        mCurrentSelectedPosition = position;
        mCurrentSelectedIndex = id;
        if (mDrawerListView != null && position != -1) {
            mDrawerListView.setItemChecked(position, true);
        }
        if (mDrawerLayout != null) {
            mDrawerLayout.closeDrawer(mFragmentContainerView);
        }
        if (mCallbacks != null) {
            mCallbacks.onDrawerItemSelected(id);
        }
    }

}
