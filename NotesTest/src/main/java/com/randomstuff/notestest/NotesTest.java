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
        //Sets the initial layout, currently based on either landscape or portrait
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
        // Handle action bar / menu item clicks here
        switch (item.getItemId()) {
            case(R.id.add_note):
                //call notefragment and make new note
                if (getFragmentManager().findFragmentById(R.id.notes) == null){
                    // this happens only in portrait mode
                    Intent i=new Intent(this, NoteActivity.class);
                    i.putExtra("id", 0L);
                    i.putExtra("index", -1);
                    startActivity(i);
                }
                else {
                    NoteFragment noteFrag = (NoteFragment)
                            getFragmentManager().findFragmentById(R.id.notes);

                    if (noteFrag == null || noteFrag.getShownIndex() != -1) {
                        noteFrag = NoteFragment.newInstance(0L, -1);

                        FragmentTransaction ft = getFragmentManager().beginTransaction();
                        ft.replace(R.id.notes, noteFrag).commit();
                    }
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

}
