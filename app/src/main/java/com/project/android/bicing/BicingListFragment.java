package com.project.android.bicing;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.project.android.bicing.data.BicingContract;
import com.project.android.bicing.sync.BicingSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */

public class BicingListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = BicingListFragment.class.getSimpleName();
    private static final int BICING_LOADER = 0;
    private static final int FAV_BICING_LOADER = 1;

    private ListView mListView;
    private TextView fav_indicator;
    private static final String SELECTED_KEY = "selected_position";
    private StationsAdapter mStationsAdapter;
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

    private int mPosition;
    private boolean all;


    private static final String[] FAV_COLUMNS = {
            BicingContract.TABLE_FAV + "." +BicingContract.FavStations.COLUMN_STATIONID};


    public interface Callback {
        public void onItemSelected(Uri dateUri);
    }


    public BicingListFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);


        /*
        BicingDBHelper h = new BicingDBHelper(getActivity(), "BicingDB", null, 1);
        h.insertFavourite(90);
        h.insertFavourite(1);
        h.insertFavourite(2);
        h.insertFavourite(3);
        h.insertFavourite(4);
        h.insertFavourite(5);
        */
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        all = prefs.getBoolean(getString(R.string.pref_key_check_all),true);
        if (all) {

            getLoaderManager().initLoader(BICING_LOADER, null, this);
        }
        else{

            getLoaderManager().initLoader(FAV_BICING_LOADER, null, this);
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //inflater.inflate(R.menu.forecastfragment, menu);
    }

    void onViewAllChanged() {
        updateBicing();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        Boolean all = prefs.getBoolean(getString(R.string.pref_key_check_all),true);
        if (all) {
            fav_indicator.setText(getText(R.string.all_indicator));
            getLoaderManager().destroyLoader(FAV_BICING_LOADER);
            getLoaderManager().restartLoader(BICING_LOADER, null, this);
        }
        else {
            fav_indicator.setText(getText(R.string.favourites_indicator));
            getLoaderManager().destroyLoader(BICING_LOADER);
            getLoaderManager().restartLoader(FAV_BICING_LOADER, null, this);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mStationsAdapter = new StationsAdapter(getActivity(), null, 0);
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        fav_indicator = (TextView) rootView.findViewById(R.id.fav_indicator);
        if (!all)
            fav_indicator.setText(getText(R.string.favourites_indicator));
        else
            fav_indicator.setText(getText(R.string.all_indicator));
        mListView = (ListView) rootView.findViewById(R.id.listview_fav_stations);
        mListView.setAdapter(mStationsAdapter);
        mListView.setEmptyView(rootView.findViewById(R.id.listview_empty));
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor = (Cursor) parent.getItemAtPosition(position);
                if (cursor != null) {
                    ((Callback) getActivity()).onItemSelected(BicingContract.Station.buildIDStationsUri(
                            (int) cursor.getLong(cursor.getColumnIndex(BicingContract.Station.COLUMN_STATIONID))));
                }
                //mPosition = position;
            }
        });
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }
        return rootView;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        mPosition = mListView.getFirstVisiblePosition();
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateBicing();
        //Toast.makeText(getActivity(), Integer.toString(count), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onResume() {
        super.onResume();

    }
    private void updateBicing() {
        BicingSyncAdapter.syncImmediately(getActivity());
        if (mPosition != ListView.INVALID_POSITION) {

            mListView.setSelection(mPosition);

        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //String sortOrder = BicingContract.Station.COLUMN_ID + " ASC";
        String sortOrder = BicingContract.Station.COLUMN_STREET + " ASC";
        Boolean all = (id == 0);

       // Log.d(LOG_TAG, bicingUri.toString());
        Intent intent = getActivity().getIntent();

        if (intent == null) {
            return null;
        }

        if (!all) {
            Uri bicingUri = BicingContract.FavStations.buildFavStationsUri();
            return new CursorLoader(
                    getActivity(),
                    bicingUri,
                    STATIONS_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }
        else {
            Uri bicingUri = BicingContract.Station.buildAllStationsUri();
            return new CursorLoader(
                    getActivity(),
                    bicingUri,
                    STATIONS_COLUMNS,
                    null,
                    null,
                    sortOrder
            );
        }


    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mStationsAdapter.swapCursor(data);
        //int count = data.getCount();
       // Toast.makeText(getActivity(), Integer.toString(count), Toast.LENGTH_LONG).show();
        if (mPosition != ListView.INVALID_POSITION) {
            mListView.setSelection(mPosition);

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mStationsAdapter.swapCursor(null);
    }
}