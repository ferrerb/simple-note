package com.randomstuff.notestest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import android.widget.ListView;
import android.database.Cursor;

public class NoteListFragment extends Fragment implements DatabaseHelper.ListListener {
    private ListView listNote;
    private DatabaseHelper dbHelper=null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState){
        View result = inflater.inflate(R.layout.notes_list, container, false);
        listNote = (ListView)result.findViewById(R.id.list);

        DatabaseHelper.getInstance(getActivity()).getListAsync();

        return(result);
    }

    @Override
    public void setList(Cursor listHere) {
        //something with listNote

    }
}
