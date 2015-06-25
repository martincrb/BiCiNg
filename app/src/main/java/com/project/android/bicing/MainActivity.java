package com.project.android.bicing;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

import com.project.android.bicing.data.BicingContract;
import com.project.android.bicing.sync.BicingSyncAdapter;


public class MainActivity extends ActionBarActivity implements BicingListFragment.Callback {
    private final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String MAPFRAGMENT_TAG = "MFTAG";
    private boolean mTwoPane;
    private boolean mViewAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        mViewAll = Utils.getViewAllPref(getApplication());
        if (findViewById(R.id.bicing_detail_container) != null) {
            mTwoPane = true;
            if (savedInstanceState == null) {
                Fragment fragment = new MapViewFragment();
                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.bicing_detail_container, fragment, MAPFRAGMENT_TAG)
                            .commit();
                }
                openAllStationsOnMap();
          //      getSupportFragmentManager().beginTransaction()
           //             .replace(R.id.bicing_detail_container, getSupportFragmentManager().findFragmentById(R.id.map), MAPFRAGMENT_TAG).commit();
            }
        }
        else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
        BicingSyncAdapter.initializeSyncAdapter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean viewall = Utils.getViewAllPref(this);
        if (mViewAll != viewall) {
            BicingListFragment ff = (BicingListFragment)getSupportFragmentManager().findFragmentById(R.id.fragment_bicing);
            if (ff != null)
                ff.onViewAllChanged();
            mViewAll = viewall;
        }
    }

    /*
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() == BicingListFragment.LOAD_STARTED)
                setProgressBarIndeterminateVisibility(true);
            else if(intent.getAction() == BicingListFragment.LOAD_FINISHED)
                setProgressBarIndeterminateVisibility(false);
        }
    };
    */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        if (id == R.id.action_map) {
            openAllStationsOnMap();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void openAllStationsOnMap() {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(MapViewFragment.DETAIL_URI, BicingContract.Station.buildAllStationsUri());

            Fragment fragment = new MapViewFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.bicing_detail_container, fragment, MAPFRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, MapsActivityv2.class)
                    .setData(BicingContract.Station.buildAllStationsUri());
            startActivity(intent);

        }
    }
    @Override
    public void onItemSelected(Uri bUri) {
        if (mTwoPane) {
            Bundle args = new Bundle();
            args.putParcelable(MapViewFragment.DETAIL_URI, bUri);

            Fragment fragment = new MapViewFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.bicing_detail_container, fragment, MAPFRAGMENT_TAG)
                    .commit();
        }
        else {
            Intent intent = new Intent(this, MapsActivityv2.class)
                    .setData(bUri);
            startActivity(intent);

        }
    }



}
