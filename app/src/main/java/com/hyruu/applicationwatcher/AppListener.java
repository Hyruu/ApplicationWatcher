package com.hyruu.applicationwatcher;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class AppListener extends Service {
    private boolean isActivityActive = true;
    private ArrayList<Intent> intentList;

    public AppListener() {
    }

    private final BroadcastReceiver package_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Log.v("package_receiver", "Broadcast received : " + action);

            if (isActivityActive) {
                sendIntent(context, intent);
            } else {
                intentList.add(intent);
            }
        }
    };

    private final BroadcastReceiver activity_receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("activity_receiver", "Broadcast received : " + action);

            if(action.equals("com.hyruu.applicationwatcher.ACTIVITY_PAUSED")){
                Log.v("activity_receiver", "Activity paused");
                isActivityActive = false;
            }
            else if(action.equals("com.hyruu.applicationwatcher.ACTIVITY_RESUMED")){
                Log.v("activity_receiver", "Activity resumed");
                isActivityActive = true;

                // Send all the intents stored during the pause to the activity
                sendAllIntents(context);
            }
        }
    };

    private void sendIntent(Context context, Intent intent) {

        // Get the Package Manager to get the application name
        // TODO Replace this part with a Hash Table
        final PackageManager pm = getApplicationContext().getPackageManager();
        ApplicationInfo ai;
        String applicationName;

        // Try to get the application name
        // TODO Database of the names since we can't get it after removal
        try {
            ai = pm.getApplicationInfo(intent.getData().getSchemeSpecificPart(), PackageManager.GET_UNINSTALLED_PACKAGES);
            applicationName = (String) pm.getApplicationLabel(ai);
            Log.v("sendIntent", "Name found");
        } catch (final PackageManager.NameNotFoundException e) {
            // Application name cannot be found, probably because application information have been deleted
            applicationName = intent.getData().getSchemeSpecificPart();
            ai = null;
            Log.v("sendIntent", "Name not found");
        }
        Log.v("sendIntent", applicationName);

        if(intent.getAction().equals("android.intent.action.PACKAGE_ADDED")){
            Log.v("sendIntent", "App installed : " + intent.getData().getSchemeSpecificPart());

            // Notify the user about the package
            Toast.makeText(context, "PACKAGE_ADDED", Toast.LENGTH_SHORT).show();
            // Notify the activity
            Intent message = new Intent("com.hyruu.applicationwatcher.PACKAGE_ADDED");
            message.putExtra("Application Name", applicationName);
            sendBroadcast(message);
        }
        else if(intent.getAction().equals("android.intent.action.PACKAGE_REMOVED")){
            Log.v("sendIntent", "App removed : " + intent.getData().getSchemeSpecificPart());

            // Notify the user about the package
            Toast.makeText(context, "PACKAGE_REMOVED", Toast.LENGTH_SHORT).show();
            // Notify the activity
            Intent message = new Intent("com.hyruu.applicationwatcher.PACKAGE_REMOVED");
            message.putExtra("Application Name", applicationName);
            sendBroadcast(message);
        }
    }

    private void sendAllIntents(Context context) {

        // Send all the stored intents to the user, and then clear the list
        for (int i=0; i<intentList.size(); i++) {
            sendIntent(context, intentList.get(i));
        }
        intentList.clear();
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        // handleCommand(intent);
        Log.v("AppListener", "Service started");

        // Initialize the intent queue (for when the activity is paused)
        intentList = new ArrayList<Intent>();

        // Register the BroadcastReceiver for package information
        IntentFilter package_filter = new IntentFilter();
        package_filter.addAction("android.intent.action.PACKAGE_ADDED");
        package_filter.addAction("android.intent.action.PACKAGE_REMOVED");
        package_filter.addDataScheme("package");
        registerReceiver(package_receiver, package_filter);

        // Register the BroadcastReceiver for activity information
        IntentFilter activity_filter = new IntentFilter();
        activity_filter.addAction("com.hyruu.applicationwatcher.ACTIVITY_PAUSED");
        activity_filter.addAction("com.hyruu.applicationwatcher.ACTIVITY_RESUMED");
        registerReceiver(activity_receiver, activity_filter);

        //sendBroadcast(new Intent("com.hyruu.applicationwatcher.PACKAGE_ADDED", Uri.parse("test")));

        // Tell the user we started the service
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        // Unregister the receivers
        unregisterReceiver(package_receiver);
        unregisterReceiver(activity_receiver);

        // Tell the user we stopped the service
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        Log.v("AppListener", "Service stopped");
    }
}
