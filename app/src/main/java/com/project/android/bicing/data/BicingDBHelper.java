package com.project.android.bicing.data;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Martin on 10/06/2015.
 */
public class BicingDBHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "stationsDB.db";

    private ContentResolver cR;
    public BicingDBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, DATABASE_NAME, factory, DATABASE_VERSION);
        cR = context.getContentResolver();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL(BicingContract.SQL_CREATE_ENTRIES_STATIONS);
        db.execSQL(BicingContract.SQL_CREATE_ENTRIES_FAVOURITES);


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(BicingContract.SQL_DELETE_ENTRIES_STATIONS);
        db.execSQL(BicingContract.SQL_DELETE_ENTRIES_FAVOURITES);
        onCreate(db);
    }

    public Cursor findStation(int id) {
        String query = "Select * FROM " + BicingContract.TABLE_FAV + " WHERE " + BicingContract.FavStations.COLUMN_STATIONID + " =  \"" + Integer.toString(id) + "\"";

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();
        return cursor;
    }

    public Cursor findFavStations() {
        String query = "SELECT * FROM favstations";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(query, null);
        cursor.moveToNext();

        return cursor;
    }

    public void insertFavourite(int id) {
        ContentValues values = new ContentValues();
        values.put(BicingContract.FavStations.COLUMN_STATIONID, Integer.valueOf(id));

        SQLiteDatabase db = this.getWritableDatabase();

        db.insert(BicingContract.TABLE_FAV, null, values);
    }




}
