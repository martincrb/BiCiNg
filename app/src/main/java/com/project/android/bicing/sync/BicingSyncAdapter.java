package com.project.android.bicing.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.project.android.bicing.BicingDataXmlParser;
import com.project.android.bicing.MainActivity;
import com.project.android.bicing.R;
import com.project.android.bicing.Utils;
import com.project.android.bicing.data.BicingContract;

import org.xmlpull.v1.XmlPullParserException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.util.List;
import java.util.Vector;

public class BicingSyncAdapter extends AbstractThreadedSyncAdapter {

    public final String LOG_TAG = BicingSyncAdapter.class.getSimpleName();

    // Interval at which to sync with the weather, in milliseconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 30; //Every 10 minutes
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;


    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 12;
    private static final int BICING_NOTIFICATION_ID = 3004;

    private static final String[] STATIONS_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying. On the other, you can search the weathertable
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            BicingContract.TABLE_STATIONS + "." + BicingContract.Station.COLUMN_ID,
            BicingContract.TABLE_STATIONS + "." + BicingContract.Station.COLUMN_STATIONID,
            BicingContract.Station.COLUMN_LAT,
            BicingContract.Station.COLUMN_LONG,
            BicingContract.Station.COLUMN_BIKES,
            BicingContract.Station.COLUMN_SLOTS,
            BicingContract.Station.COLUMN_STREET,
            BicingContract.Station.COLUMN_STREETNUMBER};


    public BicingSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    private void notifyBicing() {
        Context context = getContext();
        //checking the last update and notify if it' the first of the day
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Boolean displayNotifications = prefs.getBoolean(context.getString(R.string.pref_key_new_message_notifications),true);

        if (displayNotifications) {
            String lastNotificationKey = context.getString(R.string.pref_last_notification);
            long lastSync = prefs.getLong(lastNotificationKey, 0);

            if (System.currentTimeMillis() - lastSync >= DAY_IN_MILLIS) {
                // Last sync was more than 1 day ago, let's send a notification with the weather.

                Uri bicingUri = BicingContract.FavStations.buildFavStationsUri();

                // we'll query our contentProvider, as always
                Cursor cursor = context.getContentResolver().query(bicingUri, STATIONS_COLUMNS, null, null, null);

                if (cursor.moveToFirst()) {
                    do {
                        String bikes = cursor.getString(cursor.getColumnIndex(BicingContract.Station.COLUMN_BIKES));
                        if (Integer.parseInt(bikes) == 0) {
                            String street = cursor.getString(cursor.getColumnIndex(BicingContract.Station.COLUMN_STREET));
                            Integer StationID = cursor.getInt(cursor.getColumnIndex(BicingContract.Station.COLUMN_STATIONID));
                            String slots = cursor.getString(cursor.getColumnIndex(BicingContract.Station.COLUMN_SLOTS));
                            String title = context.getString(R.string.app_name);

                            // Define the text of the forecast.
                            String contentText = String.format(context.getString(R.string.format_notification),
                                    Utils.latin2utf(street));

                            //build your notification here.
                            NotificationCompat.Builder mBuilder =
                                    new NotificationCompat.Builder(getContext())
                                            .setSmallIcon(R.drawable.ic_bicing_notif)
                                            .setContentTitle(title)
                                            .setContentText(contentText);

                            Intent resultIntent = new Intent(context, MainActivity.class);

                            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
                            stackBuilder.addNextIntent(resultIntent);
                            PendingIntent resultPendingIntent =
                                    stackBuilder.getPendingIntent(
                                            0,
                                            PendingIntent.FLAG_UPDATE_CURRENT
                                    );
                            mBuilder.setContentIntent(resultPendingIntent);

                            NotificationManager mNotificationManager =
                                    (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
                            // WEATHER_NOTIFICATION_ID allows you to update the notification later on.
                            mNotificationManager.notify(StationID, mBuilder.build());
                            //refreshing last sync
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putLong(lastNotificationKey, System.currentTimeMillis());
                            editor.commit();
                        }

                    } while (cursor.moveToNext());
                }
            }
        }

    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "onPerformSync Called.");

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        List data = null;
        try {
            final String BICING_BASE_URL =
                    "http://wservice.viabicing.cat/v1/getstations.php?v=1";
            Uri builtUri = Uri.parse(BICING_BASE_URL).buildUpon().build();

            URL url = new URL(builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            getBicingDataFromXML(inputStream);
            notifyBicing();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error ", e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
    }

    private List getBicingDataFromXML(InputStream bicingData) {
        BicingDataXmlParser parser = new BicingDataXmlParser();
        List<BicingDataXmlParser.Station> stations = null;
        try {
            stations = parser.parse(bicingData);
            Vector<ContentValues> cVVector = new Vector<ContentValues>(stations.size());
            for (BicingDataXmlParser.Station station : stations) {
                ContentValues stationValues = new ContentValues();
                stationValues.put(BicingContract.Station.COLUMN_ID, Integer.parseInt(station.id));
                stationValues.put(BicingContract.Station.COLUMN_STATIONID, Integer.parseInt(station.id));
                stationValues.put(BicingContract.Station.COLUMN_LAT, station.lat);
                stationValues.put(BicingContract.Station.COLUMN_LONG, station.lon);
                stationValues.put(BicingContract.Station.COLUMN_STREET, station.street);
                stationValues.put(BicingContract.Station.COLUMN_SLOTS, Integer.parseInt(station.slots));
                stationValues.put(BicingContract.Station.COLUMN_BIKES, Integer.parseInt(station.bikes));
                stationValues.put(BicingContract.Station.COLUMN_STREETNUMBER, station.streetnumber);
                cVVector.add(stationValues);

            }
            if ( cVVector.size() > 0 ) {
                // Student: call bulkInsert to add the weatherEntries to the database here
                ContentValues[] cvArray = new ContentValues[cVVector.size()];
                cVVector.toArray(cvArray);
                //getContext().getContentResolver().delete(BicingContract.Station.CONTENT_URI, null, null);
                getContext().getContentResolver().bulkInsert(BicingContract.Station.CONTENT_URI, cvArray);
            }

            Log.d(LOG_TAG, "FetchBicingTask Complete. " + cVVector.size() + " Inserted");

            // String[] resultStrs = convertContentValuesToStringFormat(cVVector);
            return stations;

        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }


    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        BicingSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);

    }
}