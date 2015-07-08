package com.project.android.bicing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;

import com.project.android.bicing.data.BicingContract;

/**
 * Created by Martin on 10/06/2015.
 */
public class AddToFavDialog extends DialogFragment {
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

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        final int stationid = args.getInt("id");
        String sortOrder = BicingContract.Station.COLUMN_STREET + " ASC";
        Uri uri = BicingContract.FavStations.buildFavIDStationsUri(stationid);
        Cursor cursor = getActivity().getContentResolver().query(uri, STATIONS_COLUMNS, null, null, sortOrder);
        if (!cursor.moveToFirst()) {
            builder.setMessage(R.string.dialog_add_favourite)
                    .setPositiveButton(R.string.Add, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Uri uri = BicingContract.FavStations.buildFavStationsUri();
                            //insert to fav
                            ContentValues cv = new ContentValues();
                            cv.put(BicingContract.FavStations.COLUMN_STATIONID, stationid);
                            getActivity().getContentResolver().insert(BicingContract.FavStations.CONTENT_URI, cv);
                            getActivity().getContentResolver().notifyChange(uri, null);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
        }
        else {
            builder.setMessage(R.string.dialog_delete_favourite)
                    .setPositiveButton(R.string.Delete, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            Uri uri = BicingContract.FavStations.buildFavStationsUri();
                            //insert to fav
                            getActivity().getContentResolver().delete(uri, BicingContract.FavStations.COLUMN_STATIONID+" = "+stationid, null);
                            getActivity().getContentResolver().notifyChange(uri, null);
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });

        }
        // Create the AlertDialog object and return it
        return builder.create();
    }



}