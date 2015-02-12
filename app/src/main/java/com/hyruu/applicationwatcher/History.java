package com.hyruu.applicationwatcher;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class History extends ActionBarActivity {

    ArrayList<String> list_events;
    ArrayAdapter<String> adapter;
    ListView listview;

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.v("receiver", "Broadcast from service : " + intent.getAction());
            Log.v("receiver", intent.getStringExtra("Application Name"));

            String action = "";
            if (intent.getAction().equals("com.hyruu.applicationwatcher.PACKAGE_ADDED")) {
                action = "Installed";
            } else if (intent.getAction().equals("com.hyruu.applicationwatcher.PACKAGE_REMOVED")) {
                action = "Removed";
            }

            addListElement(action + " : " + intent.getStringExtra("Application Name"));
        }
    };

    private void addListElement(String element) {
        // Add the element to the data list
        list_events.add(element);
        // Notify the adapter that the list changed
        adapter.notifyDataSetChanged();
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // Initialize the event list
        list_events = new ArrayList<String>();
        listview = (ListView) findViewById(R.id.listView);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                list_events);
        listview.setAdapter(adapter);
    }

    @Override
    public void onDestroy() {

        // Stop the service when the application is definitively closed
        stopService(new Intent(this, AppListener.class));

        // Don't forget to call the original onDestroy()
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Notify the service the activity is resumed and have it send all the stored information
        sendBroadcast(new Intent("com.hyruu.applicationwatcher.ACTIVITY_RESUMED"));

        // Register the receiver again
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.hyruu.applicationwatcher.PACKAGE_ADDED");
        filter.addAction("com.hyruu.applicationwatcher.PACKAGE_REMOVED");
        registerReceiver(receiver, filter);
    }

    @Override
    protected void onPause() {

        // Pause the receiver, actually unregistering it
        unregisterReceiver(receiver);

        // Notify the service the activity is paused and will not receive any sent information
        sendBroadcast(new Intent("com.hyruu.applicationwatcher.ACTIVITY_PAUSED"));

        super.onPause();
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

    private class StableArrayAdapter extends ArrayAdapter<String> {

        HashMap<String, Integer> mIdMap = new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId,
                                  List<String> objects) {
            super(context, textViewResourceId, objects);
            for (int i = 0; i < objects.size(); ++i) {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item = getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
