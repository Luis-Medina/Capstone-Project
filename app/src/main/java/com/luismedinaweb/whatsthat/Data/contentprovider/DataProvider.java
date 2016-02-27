package com.luismedinaweb.whatsthat.Data.contentprovider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.text.TextUtils;
import android.util.Log;

import java.io.FileNotFoundException;

/**
 * Created by Luis on 2/3/2015.
 */
public class DataProvider extends ContentProvider {

    private DataDatabase mOpenHelper;

    private static String TAG = DataProvider.class.getSimpleName();
    private static final UriMatcher sUriMatcher = buildUriMatcher();

    private static final int PHOTOS = 100;
    private static final int PHOTOS_ID = 101;
    private static final int RESULTS = 102;
    private static final int RESULTS_ID = 103;

    private static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = DataContract.CONTENT_AUTHORITY;
        matcher.addURI(authority, "photos", PHOTOS);
        matcher.addURI(authority, "photos/*", PHOTOS_ID);
        matcher.addURI(authority, "results", RESULTS);
        matcher.addURI(authority, "results/*", RESULTS_ID);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new DataDatabase(getContext());
        return true;
    }

    private String getTable(int tableID) {
        switch (tableID) {
            case PHOTOS:
            case PHOTOS_ID:
                return DataDatabase.Tables.PHOTOS;
            case RESULTS:
            case RESULTS_ID:
                return DataDatabase.Tables.RESULTS;
            default:
                throw new IllegalArgumentException("Unknown table");
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) throws IllegalArgumentException {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(getTable(match));
        switch (match) {
            case PHOTOS:
            case RESULTS:
                break;
            case PHOTOS_ID:
            case RESULTS_ID:
                String id = DataContract.DefaultTableClass.getTableUri(uri);
                queryBuilder.appendWhere(BaseColumns._ID + "=" + id);
                break;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
        Cursor cursor = queryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);
        if (match == PHOTOS || match == PHOTOS_ID) {
            if (getContext() != null)
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }
        return cursor;
    }

    @Override
    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        return super.openFile(uri, mode);
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) throws IllegalArgumentException, SQLiteConstraintException {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        String tableName = getTable(match);
        long recordId;
        try {
            recordId = db.insertWithOnConflict(tableName, null, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
            if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
            return DataContract.DefaultTableClass.buildTableUri(tableName, String.valueOf(recordId));
        } catch (SQLiteException e) {
            throw e;
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) throws IllegalArgumentException {
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        String selectionCriteria = selection;
        switch (match) {
            case PHOTOS:
            case RESULTS:
                return db.update(getTable(match), contentValues, selectionCriteria, selectionArgs);
            case PHOTOS_ID:
            case RESULTS_ID:
                String id = DataContract.DefaultTableClass.getTableUri(uri);
                selectionCriteria = BaseColumns._ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                return db.update(getTable(match), contentValues, selectionCriteria, selectionArgs);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.v(TAG, "delete(uri=" + uri);
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PHOTOS:
                int deleted3 = 0;
                try {
                    db.delete(getTable(match), null, null);
                    db.delete("SQLITE_SEQUENCE", "Name = ?", new String[]{getTable(PHOTOS)});
                    db.delete(getTable(RESULTS), null, null);
                    if (getContext() != null)
                        getContext().getContentResolver().notifyChange(uri, null);
                } catch (SQLiteException ex) {
                    Log.e(TAG, ex.getMessage());
                }
                return deleted3;
            case RESULTS:
                return db.delete(getTable(match), selection, selectionArgs);
            case PHOTOS_ID:
            case RESULTS_ID:
                String id = DataContract.DefaultTableClass.getTableUri(uri);
                selection = BaseColumns._ID + "=" + id
                        + (!TextUtils.isEmpty(selection) ? " AND (" + selection + ")" : "");
                int deleted = db.delete(getTable(match), selection, selectionArgs);
                if (getContext() != null) getContext().getContentResolver().notifyChange(uri, null);
                return deleted;
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        String tableName = getTable(match);
        switch (match) {
            case PHOTOS:
            case RESULTS:
                return DataContract.DefaultTableClass.getContentType(tableName, DataContract.DefaultTableClass.CONTENT_TYPE);
            case PHOTOS_ID:
            case RESULTS_ID:
                return DataContract.DefaultTableClass.getContentType(tableName, DataContract.DefaultTableClass.CONTENT_ITEM_TYPE);
            default:
                throw new IllegalArgumentException("Unknown Uri: " + uri);
        }
    }

}
