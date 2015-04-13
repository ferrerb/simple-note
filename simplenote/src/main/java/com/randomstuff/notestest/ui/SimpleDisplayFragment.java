package com.randomstuff.notestest.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.randomstuff.notestest.R;

import java.io.InputStream;
/** Shows some basic information retrieved from text files */
public class SimpleDisplayFragment extends Fragment {
    private TextView mText = null;

    @Override
    public View onCreateView (LayoutInflater inflater, ViewGroup v, Bundle savedInstanceState) {
        View result = inflater.inflate(R.layout.simple_display, v, false);

        mText = (TextView) result.findViewById(R.id.simple_text);
        readStuff(getArguments().getString("file"));

        return result;
    }

    /** Displays a simple text file in a text view */
    private void readStuff(String s) {
        try {
            InputStream input = getActivity().getAssets().open("misc/" + s);

            int size = input.available();
            byte[] buffer = new byte[size];
            input.read(buffer);
            input.close();

            String mBufferText = new String(buffer);

            mText.setText(mBufferText);
        }
        catch (java.io.IOException e) {
            e.printStackTrace();
        }

    }
}
