package com.example.nicholas.myapplication.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import com.example.nicholas.myapplication.Shelter;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

public class SQLiteHandler extends SQLiteOpenHelper {
    private static final String TAG = SQLiteHandler.class.getSimpleName();

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 5;

    // Database Name
    private static final String DATABASE_NAME = "android_homefull";

    // Shelters Table
    private static final String TABLE = "shelters";

    // Login Table Columns names
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_LONG = "longitude";
    private static final String KEY_LAT = "latitude";
    private static final String KEY_DISTANCE = "distance";


    public SQLiteHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creating Tables
    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_SHELTER_TABLE = "CREATE TABLE " + TABLE + "("
                + KEY_ID + " INTEGER PRIMARY KEY,"
                + KEY_LONG + " TEXT,"
                + KEY_LAT + " TEXT,"
                + KEY_DISTANCE + " TEXT,"
                + KEY_NAME + " TEXT" + ")";
        db.execSQL(CREATE_SHELTER_TABLE);
        Log.d(TAG, "Database tables created");
    }

    // Upgrading database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older table if existed
        Log.d(TAG, "Upgrading the Database");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        // Create tables again
        onCreate(db);
    }

    /**
     * Storing user details in database
     * */
    public void addShelter(String name, LatLng latLng) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(KEY_NAME, name); // Name
        values.put(KEY_LAT, latLng.latitude);
        values.put(KEY_LONG, latLng.longitude);
        // Inserting Row
        long id = db.insert(TABLE, null, values);
        db.close(); // Closing database connection

        Log.d(TAG, "New shelter inserted: " + name);
    }

    /**
     * Getting user data from database
     * */
    public ArrayList<Shelter> getShelterDetails() {
        ArrayList<Shelter> shelters = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE, null);

        // Move to first row
        cursor.moveToFirst();

        if (cursor.getCount() > 0) {
            long lat = Long.valueOf(getColumn(cursor, KEY_LAT));
            long lng =  Long.valueOf(getColumn(cursor, KEY_LONG));
            long dist = Long.valueOf(getColumn(cursor, KEY_DISTANCE));
            Shelter shelter = new Shelter(getColumn(cursor,"name"),lat,lng,dist);
            shelters.add(shelter);
        }
        cursor.close();
        db.close();
        // return user
        Log.d(TAG, "Fetching shelters from SQLITE: "+ shelters.toArray().toString());

        return shelters;
    }

    public String getColumn(Cursor cursor, String col){
        int index = cursor.getColumnIndex(col);
        cursor.moveToNext();
        if (index == -1) {
            System.out.println("getColumnIndex() couldn't find "+ col);
            return null;
        } else return cursor.getString(index);
    }

    /**
     * Re crate database Delete all tables and create them again
     * */
    public void deleteShelters() {
        SQLiteDatabase db = getWritableDatabase();
        // Delete All Rows
        db.delete(TABLE, null, null);
        db.close();
        Log.d(TAG, "Deleted all user info from sqlite");
    }

}