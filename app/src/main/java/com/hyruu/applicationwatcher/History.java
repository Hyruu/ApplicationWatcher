package com.hyruu.applicationwatcher;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;


public class History extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_history, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void toggleAppListener(View view) {

        //Check the toggle button state
        boolean isToggleOn = ((ToggleButton) view).isChecked();

        if (isToggleOn) {
            // Start the service
            startService(new Intent(this, AppListener.class));
        } else {
            // Stop the service
            stopService(new Intent(this, AppListener.class));
        }
    }

    @Override
    public void onDestroy() {
        // Stop the service when the application is definitively closed
        stopService(new Intent(this, AppListener.class));
        // Don't forget to call the original onDestroy()
        super.onDestroy();
    }
}
