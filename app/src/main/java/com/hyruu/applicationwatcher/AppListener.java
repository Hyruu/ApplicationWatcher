package com.hyruu.applicationwatcher;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class AppListener extends Service {
    public AppListener() {
    }

    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v("log", "Broadcast received");

            final PackageManager pm = getApplicationContext().getPackageManager();
            ApplicationInfo ai;
            String applicationName;
            try {
                ai = pm.getApplicationInfo(intent.getData().getSchemeSpecificPart(), PackageManager.GET_UNINSTALLED_PACKAGES);
                applicationName = (String) pm.getApplicationLabel(ai);
                Log.v("log", "Name found");
            } catch (final PackageManager.NameNotFoundException e) {
                applicationName = intent.getData().getSchemeSpecificPart();
                ai = null;
                Log.v("log", "Name not found");
            }
            Log.v("log", applicationName);

            if(action.equals("android.intent.action.PACKAGE_ADDED")){
                Log.v("log", "App installed : " + intent.getData().getSchemeSpecificPart());
                Toast.makeText(context, "PACKAGE_ADDED", Toast.LENGTH_SHORT).show();
            }
            else if(action.equals("android.intent.action.PACKAGE_REMOVED")){
                Log.v("log", "App removed : " + intent.getData().getSchemeSpecificPart());
                Toast.makeText(context, "PACKAGE_REMOVED", Toast.LENGTH_SHORT).show();
            }
        }
    };

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // handleCommand(intent);
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        Log.v("log", "Service started");

        //TODO Watch for application updates
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");

        registerReceiver(receiver, filter);


        //TODO Pop Toasts for the user and return the info to the app

        // Tell the user we stopped.
        Toast.makeText(this, "Service started", Toast.LENGTH_SHORT).show();

        return START_STICKY;
    }

    @Override
    public void onDestroy() {

        unregisterReceiver(receiver);
        // Tell the user we stopped.
        Toast.makeText(this, "Service stopped", Toast.LENGTH_SHORT).show();
        Log.v("log", "Service stopped");
    }
}
