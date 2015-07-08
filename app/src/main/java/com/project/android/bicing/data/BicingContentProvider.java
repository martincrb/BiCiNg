package com.project.android.bicing.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;

/**
 * Created by Martin on 10/06/2015.
 */
public class BicingContentProvider extends ContentProvider{

    private static final String LOG_TAG = BicingContentProvider.class.getSimpleName();

    private static final UriMatcher sURIMatcher = buildUriMatcher();
    private BicingDBHelper DB;

    public static final int STATIONS = 1;
    public static final int STATIONS_ID = 2;
    public static final int STATIONS_FAV = 3;
    public static final int STATIONS_FAV_ID = 4;

    private static final SQLiteQueryBuilder sFavouriteStationsQueryBuilder;
    static{
        sFavouriteStationsQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
        sFavouriteStationsQueryBuilder.setTables(
                BicingContract.TABLE_STATIONS + " INNER JOIN " +
                        BicingContract.TABLE_FAV +
                        " ON " + BicingContract.TABLE_STATIONS +
                        "." + BicingContract.Station.COLUMN_STATIONID +
                        " = " + BicingContract.TABLE_FAV +
                        "." + BicingContract.FavStations.COLUMN_STATIONID);
    }

    //stations.stationid = ?
    private static final String sStationWithIdSelection =
            BicingContract.TABLE_STATIONS+
                    "." + BicingContract.Station.COLUMN_STATIONID + " = ? ";

    private Cursor getStationById(Uri uri, String[] projection, String sortOrder) {
        String id = BicingContract.Station.getStationIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{id};
        selection = sStationWithIdSelection;

        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(BicingContract.TABLE_STATIONS);
        Cursor ret = queryBuilder.query(DB.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
        return ret;
    }

    private Cursor getFavStationById(Uri uri, String[] projection, String sortOrder) {
        String id = BicingContract.FavStations.getStationIdFromUri(uri);

        String[] selectionArgs;
        String selection;

        selectionArgs = new String[]{id};
        selection = sStationWithIdSelection;

        return sFavouriteStationsQueryBuilder.query(DB.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getFavStations(Uri uri, String[] projection, String sortOrder) {

        //Log.d(LOG_TAG + " getFavStations", uri.toString());
        Cursor ret = sFavouriteStationsQueryBuilder.query(DB.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );
        ret.moveToNext();
        return ret;
    }

    private Cursor getStations(Uri uri, String[] projection, String sortOrder) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(BicingContract.TABLE_STATIONS);
       // Log.d(LOG_TAG + " getFavStations", uri.toString());
        Cursor ret = queryBuilder.query(DB.getReadableDatabase(),
                projection,
                null,
                null,
                null,
                null,
                sortOrder
        );

        return ret;
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = BicingContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, BicingContract.PATH_STATIONS, STATIONS);
        matcher.addURI(authority, BicingContract.PATH_STATIONS + "/#", STATIONS_ID);
        matcher.addURI(authority, BicingContract.PATH_STATIONS + "/" + BicingContract.PATH_FAV, STATIONS_FAV);
        matcher.addURI(authority, BicingContract.PATH_STATIONS + "/" + BicingContract.PATH_FAV+ "/#", STATIONS_FAV_ID);
        // 3) Return the new matcher!
        return matcher;
    }

    @Override
    public boolean onCreate() {
        DB = new BicingDBHelper(getContext(), null, null, 1);
        return false;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor = null;
        int uriType = sURIMatcher.match(uri);
       // Log.d(LOG_TAG, "URI: " + uri.toString() + " TYPE: " + Integer.toString(uriType));
        switch (uriType) {
            // stations/#
            case STATIONS_ID: {
                retCursor = getStationById(uri, projection, sortOrder);
                break;
            }
            // stations
            case STATIONS: {
                retCursor = getStations(uri, projection, sortOrder);
                break;
            }
            // stations/favourites
            case STATIONS_FAV: {
                retCursor = getFavStations(uri, projection, sortOrder);
              //  Log.d(LOG_TAG + " cursor count", Integer.toString(retCursor.getCount()));
                break;
            }

            // stations/favourites/#
            case STATIONS_FAV_ID: {
                retCursor = getFavStationById(uri, projection, sortOrder);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case STATIONS_ID:
                return BicingContract.Station.CONTENT_ITEM_TYPE;
            case STATIONS_FAV:
                return BicingContract.FavStations.CONTENT_TYPE;
            case STATIONS:
                return BicingContract.Station.CONTENT_TYPE;
            case STATIONS_FAV_ID:
                return BicingContract.FavStations.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriType = sURIMatcher.match(uri);

        SQLiteDatabase sqlDB = DB.getWritableDatabase();
        Uri returnUri;
        switch (uriType) {
            case STATIONS: {
                long _id = sqlDB.insert(BicingContract.TABLE_STATIONS, null, values);
                if (_id > 0)
                    returnUri = BicingContract.Station.buildIDStationsUri((int) _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STATIONS_FAV: {
                long _id = sqlDB.insert(BicingContract.TABLE_FAV, null, values);
                if (_id > 0)
                  //  returnUri = BicingContract.Station.buildFavIDStationsUri((int) _id);
                    returnUri = BicingContract.FavStations.buildFavIDStationsUri((int) _id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STATIONS_FAV_ID: {
                long _id = sqlDB.insert(BicingContract.TABLE_FAV, null, values);
                if (_id > 0)
                    returnUri = BicingContract.FavStations.buildFavIDStationsUri(((int) _id));
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = DB.getWritableDatabase();
        int rowsDeleted = 0;
        String id;
        switch (uriType) {
            case STATIONS:
                rowsDeleted = sqlDB.delete(BicingContract.TABLE_STATIONS,
                        selection,
                        selectionArgs);
                break;
            case STATIONS_FAV: {
                rowsDeleted = sqlDB.delete(BicingContract.TABLE_FAV,
                        selection,
                        selectionArgs);
                break;
            }
            case STATIONS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(BicingContract.TABLE_STATIONS,
                            BicingContract.Station.COLUMN_STATIONID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(BicingContract.TABLE_STATIONS,
                            BicingContract.Station.COLUMN_STATIONID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            case STATIONS_FAV_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsDeleted = sqlDB.delete(BicingContract.TABLE_FAV,
                            BicingContract.Station.COLUMN_STATIONID + "=" + id,
                            null);
                } else {
                    rowsDeleted = sqlDB.delete(BicingContract.TABLE_FAV,
                            BicingContract.Station.COLUMN_STATIONID + "=" + id
                                    + " and " + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (selection == null || rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int uriType = sURIMatcher.match(uri);
        SQLiteDatabase sqlDB = DB.getWritableDatabase();
        int rowsUpdated = 0;
        String id;
        switch (uriType) {
            case STATIONS:
                rowsUpdated = sqlDB.update(BicingContract.TABLE_STATIONS,
                        values,
                        selection,
                        selectionArgs);
                break;
            case STATIONS_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(BicingContract.TABLE_STATIONS,
                                    values,
                                    BicingContract.Station.COLUMN_STATIONID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(BicingContract.TABLE_STATIONS,
                                    values,
                                    BicingContract.Station.COLUMN_STATIONID + "=" + id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;

            case STATIONS_FAV_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated =
                            sqlDB.update(BicingContract.TABLE_FAV,
                                    values,
                                    BicingContract.Station.COLUMN_STATIONID + "=" + id,
                                    null);
                } else {
                    rowsUpdated =
                            sqlDB.update(BicingContract.TABLE_FAV,
                                    values,
                                    BicingContract.Station.COLUMN_STATIONID + "=" + id
                                            + " and "
                                            + selection,
                                    selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = DB.getWritableDatabase();
        final int match = sURIMatcher.match(uri);
        switch (match) {
            case STATIONS:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(BicingContract.TABLE_STATIONS, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

}
