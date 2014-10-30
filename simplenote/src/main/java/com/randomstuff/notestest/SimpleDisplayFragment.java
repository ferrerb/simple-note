package com.randomstuff.notestest;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.io.InputStream;

public class SimpleDisplayFragment extends Fragment {
    private TextView mText = null;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.simple_display, v, false);

        mText = (TextView) result.findViewById(R.id.simple_text);
        readStuff(getArguments().getString("file"));

        return result;
    }

    private void readStuff(String s) {
        try {
            InputStream input = getActivity().getAssets().open("misc/" + s);

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            String text = new String(buffer);

            mText.setText(text);
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }
}
