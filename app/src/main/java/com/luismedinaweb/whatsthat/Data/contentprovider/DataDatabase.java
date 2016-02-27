package com.luismedinaweb.whatsthat.Data.contentprovider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

/**
 * Created by Luis on 2/3/2015.
 */
public class DataDatabase extends SQLiteOpenHelper {

    interface Tables {
        String PHOTOS = "photos";
        String RESULTS = "results";
    }

    private static final String DATABASE_NAME = "whatsthat.db";
    private static final int DATABASE_VERSION = 1;

    public DataDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String photosTable = "CREATE TABLE " + Tables.PHOTOS + "("
                + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + DataContract.PhotosColumns.PHOTO_DATE + " INTEGER NOT NULL,"
                + DataContract.PhotosColumns.PHOTO_PATH + " TEXT NOT NULL,"
                + "UNIQUE(" + DataContract.PhotosColumns.PHOTO_DATE + ", "
                + DataContract.PhotosColumns.PHOTO_PATH + ") )";
        sqLiteDatabase.execSQL(photosTable);

        String resultsTable = "CREATE TABLE " + Tables.RESULTS + "("
                + DataContract.ResultsColumns.PHOTO_ID + " ID NOT NULL,"
                + DataContract.ResultsColumns.RESULT_LABEL + " TEXT NOT NULL,"
                + DataContract.ResultsColumns.RESULT_SCORE + " REAL NOT NULL,"
                + "UNIQUE(" + DataContract.ResultsColumns.PHOTO_ID + ", "
                + DataContract.ResultsColumns.RESULT_LABEL + ") )";
        sqLiteDatabase.execSQL(resultsTable);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        if (oldVersion != DATABASE_VERSION) {
            throw new SQLiteException("Database version mismatch!");
        }
    }

}
