package com.project.android.bicing.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Martin on 09/06/2015.
 */
public class BicingContract {

    private static final String LOG_TAG = BicingContract.class.getSimpleName();

    public BicingContract() {};

    public static final String CONTENT_AUTHORITY = "com.project.android.bicing";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_STATIONS = "stations";
    public static final String PATH_FAV = "favourites";

    public static final String TABLE_STATIONS = "stations";
    public static final String TABLE_FAV = "favstations";

    private static final String TEXT_TYPE = " TEXT";
    private static final String COMMA_SEP = ",";

    public static final String SQL_CREATE_ENTRIES_STATIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_STATIONS + " (" +
                    Station.COLUMN_ID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE," +
                    Station.COLUMN_STATIONID + " INTEGER NOT NULL" + COMMA_SEP +
                    Station.COLUMN_LAT + TEXT_TYPE + COMMA_SEP +
                    Station.COLUMN_LONG + TEXT_TYPE + COMMA_SEP +
                    Station.COLUMN_BIKES + " INTEGER" + COMMA_SEP +
                    Station.COLUMN_SLOTS + " INTEGER" + COMMA_SEP +
                    Station.COLUMN_STREET + TEXT_TYPE + COMMA_SEP +
                    Station.COLUMN_STREETNUMBER + TEXT_TYPE + COMMA_SEP +
                    " FOREIGN KEY (" + Station.COLUMN_STATIONID + ") REFERENCES " +
                    TABLE_FAV + " (" + FavStations.COLUMN_STATIONID + ") "+
                    ");";

    public static final String SQL_CREATE_ENTRIES_FAVOURITES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_FAV + " (" +
                    FavStations.COLUMN_STATIONID + " INTEGER PRIMARY KEY ON CONFLICT REPLACE"+
                    " );";

    public static final String SQL_DELETE_ENTRIES_STATIONS = "DROP TABLE IF EXISTS " + TABLE_STATIONS;
    public static final String SQL_DELETE_ENTRIES_FAVOURITES = "DROP TABLE IF EXISTS " + TABLE_FAV;

    public static abstract class FavStations implements BaseColumns {
        public static final String COLUMN_STATIONID = "stationid";
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_FAV;

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATIONS).appendPath(PATH_FAV).build();

        public static Uri buildFavStationsUri() {
            Uri result = CONTENT_URI.buildUpon().build();
            //Log.d(LOG_TAG, "buildFavStationsUri" + result.toString());
            return result;
        }
        public static Uri buildFavIDStationsUri(int id) {
            Uri result = CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
            //  Log.d(LOG_TAG, "buildFavStationsUri" + result.toString());
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

    public static abstract class Station implements BaseColumns {

        public static final String COLUMN_ID = "_id";
        public static final String COLUMN_STATIONID = "stationid";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LONG = "long";
        public static final String COLUMN_BIKES = "bikes";
        public static final String COLUMN_SLOTS = "slots";
        public static final String COLUMN_STREET = "street";
        public static final String COLUMN_STREETNUMBER = "streetnumber";

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STATIONS).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATIONS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STATIONS;


        public static String getStationIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri buildAllStationsUri() {
            Uri result = CONTENT_URI.buildUpon().build();
            //Log.d(LOG_TAG, "buildAllStationsUri" + result.toString());
            return result;
        }
        public static Uri buildIDStationsUri(int id) {
            Uri result = CONTENT_URI.buildUpon().appendPath(Integer.toString(id)).build();
            //Log.d(LOG_TAG, "buildIDStationsUri" + result.toString());
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

    }

}
