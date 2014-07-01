package com.randomstuff.notestest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.FileReader;

public class SimpleDisplayFragment extends Fragment {
    private TextView mText = null;

    static SimpleDisplayFragment newInstance(String s) {
        SimpleDisplayFragment frag = new SimpleDisplayFragment();

        Bundle args = new Bundle();
        args.putString("file", s);
        frag.setArguments(args);

        return frag;
    }

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.simple_display, v, false);

        mText = (TextView) result.findViewById(R.id.simple_text);
        readStuff(getArguments().getString("file"));

        return result;
    }

    private void readStuff(String s) {
        try {
            FileReader fr = new FileReader(getActivity().getAssets().open(s));
        }
        catch (java.io.IOException e) {

        }
        finally {

        }
    }
}
