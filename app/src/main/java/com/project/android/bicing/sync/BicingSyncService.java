package com.project.android.bicing.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class BicingSyncService extends Service {
    private static final Object sSyncAdapterLock = new Object();
    private static BicingSyncAdapter sSunshineSyncAdapter = null;

    @Override
    public void onCreate() {
        Log.d("BicingSyncService", "onCreate - BicingSyncService");
        synchronized (sSyncAdapterLock) {
            if (sSunshineSyncAdapter == null) {
                sSunshineSyncAdapter = new BicingSyncAdapter(getApplicationContext(), true);
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return sSunshineSyncAdapter.getSyncAdapterBinder();
    }
}