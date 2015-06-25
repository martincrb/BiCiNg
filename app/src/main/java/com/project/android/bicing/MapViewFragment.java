package com.project.android.bicing;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.project.android.bicing.data.BicingContract;

import java.util.ArrayList;

/**
 * Created by Martin on 11/06/2015.
 */
public class MapViewFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private GoogleMap map;

    private String currentStreet;
    private static View view;

    private ShareActionProvider mShareActionProvider;

    private static final String BICING_SHARE_HASHTAG = " #BicingApp";
    private static  String  MAPFRAGMENT_TAG = "MapFragment";
    static final String DETAIL_URI = "URI";
    private LatLng BARCELONA = new LatLng(41.383333, 2.183333);
    private LatLng SELECTED_STATION;
    private static final String LOG_TAG = MapsActivityv2.class.getSimpleName();

    private static final String[] STATIONS_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying. On the other, you can search the weathertable
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            BicingContract.TABLE_STATIONS + "." + BicingContract.Station.COLUMN_ID,
            BicingContract.Station.COLUMN_STATIONID,
            BicingContract.Station.COLUMN_LAT,
            BicingContract.Station.COLUMN_LONG,
            BicingContract.Station.COLUMN_BIKES,
            BicingContract.Station.COLUMN_SLOTS,
            BicingContract.Station.COLUMN_STREET,
            BicingContract.Station.COLUMN_STREETNUMBER
    };


    private Uri mUri;
    private ArrayList<MarkerOptions> markers;
    private ArrayList<Marker> markers2;
    private ArrayList<Integer> markersId;
    public MapViewFragment() {
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_maps_activityv2, container, false);
            Bundle arguments = getArguments();
            if (arguments != null) {
                mUri = arguments.getParcelable(MapViewFragment.DETAIL_URI);
            }
        }
        catch (InflateException e) {

        }


        markers = new ArrayList<MarkerOptions>();
        markersId = new ArrayList<Integer>();
        markers2 = new ArrayList<Marker>();
        getLoaderManager().initLoader(0, null, this);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_maps_activityv2, menu);

        MenuItem item = menu.findItem(R.id.action_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        mShareActionProvider.setShareIntent(createShareForecastIntent());
    }

    private Intent createShareForecastIntent() {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        shareIntent.setType("text/plain");
        if (currentStreet == null) currentStreet = "some place";
        shareIntent.putExtra(Intent.EXTRA_TEXT, "Im gonna grab a bike at " + currentStreet + "! U wanna come?" + BICING_SHARE_HASHTAG);
        return shareIntent;
    }



    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (id == 0) {
            if (mUri != null) {
                Log.d("MAPS ACTIVITY", "URI OK " + mUri.toString());
                return new CursorLoader(
                        getActivity(),
                        mUri,
                        STATIONS_COLUMNS,
                        null,
                        null,
                        null
                );
            }
        }
        Log.e("MAPS ACTIVITY", "URI NULL");
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

                if (data != null && data.moveToFirst()) {
                    do {
                        String st =  data.getString(data.getColumnIndex(BicingContract.Station.COLUMN_STREET));
                        String street = Utils.latin2utf(st);
                        String latStr = data.getString(data.getColumnIndex(BicingContract.Station.COLUMN_LAT));
                        String lonStr = data.getString(data.getColumnIndex(BicingContract.Station.COLUMN_LONG));
                        String bikesStr = data.getString(data.getColumnIndex(BicingContract.Station.COLUMN_BIKES));
                        String slotsStr = data.getString(data.getColumnIndex(BicingContract.Station.COLUMN_SLOTS));
                        int stID = data.getInt(data.getColumnIndex(BicingContract.Station.COLUMN_STATIONID));

                        String snippetStr = "Bicis: "+bikesStr+" Slots: "+slotsStr;
                        float latf = Float.parseFloat(latStr);
                        float lonf = Float.parseFloat(lonStr);
                        SELECTED_STATION = new LatLng(latf, lonf);
                        // map.moveCamera(CameraUpdateFactory.newLatLngZoom(SELECTED_STATION, 13));
                        markersId.add(stID);
                        markers.add(new MarkerOptions()
                                .position(new LatLng(latf, lonf))
                                .title(street)
                                .snippet(snippetStr));
                    }
                    while (data.moveToNext());
                }

        if (mShareActionProvider != null) {
            mShareActionProvider.setShareIntent(createShareForecastIntent());
        }
        setUpMapIfNeeded();
    }
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (map == null) {
            // Try to obtain the map from the SupportMapFragment.
            map = ((SupportMapFragment) getChildFragmentManager()
                    .findFragmentById(R.id.map)).getMap();
            // Check if we were successful in obtaining the map.
            if (map != null) {
                setUpMap();
            }
        }
    }

    private void setUpMap() {
        //   mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        if (SELECTED_STATION != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(SELECTED_STATION, 13));
        }
        else {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(BARCELONA, 13));
        }
        for (MarkerOptions mopt : markers) {
            Marker marker = map.addMarker(mopt);

            marker.showInfoWindow();
            markers2.add(marker);

        }
        map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                currentStreet = marker.getTitle();
                return false;
            }
        });
        map.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                AddToFavDialog favDialog = new AddToFavDialog();
                Bundle args = new Bundle();
                int stidx = markers2.indexOf(marker);
                args.putInt("id", markersId.get(stidx));
                favDialog.setArguments(args);
                favDialog.show(getActivity().getFragmentManager(), MAPFRAGMENT_TAG);
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
