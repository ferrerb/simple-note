package com.randomstuff.notestest;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class NotesTest extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.options, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        switch (item.getItemId()) {
            case(R.id.add_note):
                //call notefragment and make new note
                if (getFragmentManager().findFragmentById(R.id.notes) == null){
                    Intent i=new Intent(this, NoteActivity.class);
                    i.putExtra("id", -1L);
                    i.putExtra("index", -1);
                    startActivity(i);
                }
                else {
                    NoteFragment noteFrag = (NoteFragment)
                            getFragmentManager().findFragmentById(R.id.notes);

                    if (noteFrag == null || noteFrag.getShownIndex() != -1) {
                        noteFrag = NoteFragment.newInstance(-1L, -1);

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.notes, noteFrag).commit();
                    }
                }
                return true;
            case(R.id.settings):
                return true;
            case(R.id.help):
                return true;
            case(R.id.about):
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
