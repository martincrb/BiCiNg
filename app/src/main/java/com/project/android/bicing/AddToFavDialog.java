package com.project.android.bicing;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;

import com.project.android.bicing.data.BicingContract;

/**
 * Created by Martin on 10/06/2015.
 */
public class AddToFavDialog extends DialogFragment {
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        Bundle args = getArguments();
        final int stationid = args.getInt("id");

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
        // Create the AlertDialog object and return it
        return builder.create();
    }



}