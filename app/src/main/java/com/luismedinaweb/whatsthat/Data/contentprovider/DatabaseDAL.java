package com.luismedinaweb.whatsthat.Data.contentprovider;

import android.app.IntentService;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.util.Log;

import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.Data.model.base.Result;

import java.util.ArrayList;

/**
 * Created by Luis on 2/4/2015.
 */
public class DatabaseDAL extends IntentService {

    private static final String TAG = DatabaseDAL.class.getSimpleName();

    public static final String GET_PHOTOS = "get_photos";
    public static final String GET_RESULTS = "get_results";
    public static final String ADD_PHOTO = "add_photo";
    public static final String CLEAR_DATABASE = "clear_database";
    public static final String DELETE_PHOTO = "delete_photo";

    public static final String KEY_PHOTO = "photo";
    public static final String KEY_PHOTO_ID = "photo_id";

    public static final long INVALID_PHOTO_ID = -1;


    public static ArrayList<Photo> getPhotos(Context context) {
        ArrayList<Photo> toReturn = new ArrayList<>();
        Cursor cursor = null;
        try {
            Uri uri = DataContract.Photos.CONTENT_URI;
            String[] projection = new String[]{
                    DataContract.Photos._ID,
                    DataContract.Photos.PHOTO_DATE,
                    DataContract.Photos.PHOTO_PATH
            };

            cursor = context.getContentResolver().query(uri, projection, null, null, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Photo thisPhoto = new Photo();
                    thisPhoto.setId(cursor.getInt(cursor.getColumnIndex(DataContract.Photos._ID)));
                    thisPhoto.setDate(cursor.getLong(cursor.getColumnIndex(DataContract.Photos.PHOTO_DATE)));
                    thisPhoto.setPhotoPath(cursor.getString(cursor.getColumnIndex(DataContract.Photos.PHOTO_PATH)));

                    //thisPhoto.setResults(getResults(context, thisPhoto.getId()));

                    toReturn.add(thisPhoto);
                }
            }
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return toReturn;
    }

    public static ArrayList<Result> getResults(Context context, long id) {
        ArrayList<Result> toReturn = new ArrayList<>();
        Cursor cursor = null;
        try {
            Uri uri = DataContract.Results.CONTENT_URI;
            String[] projection = new String[]{
                    DataContract.Results.PHOTO_ID,
                    DataContract.Results.RESULT_LABEL,
                    DataContract.Results.RESULT_SCORE
            };

            String selection = DataContract.Results.PHOTO_ID + " = ? ";
            String[] selectionArgs = {String.valueOf(id)};

            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    Result thisResult = new Result();
                    thisResult.setPhotoId(cursor.getInt(cursor.getColumnIndex(DataContract.Results.PHOTO_ID)));
                    thisResult.setLabel(cursor.getString(cursor.getColumnIndex(DataContract.Results.RESULT_LABEL)));
                    thisResult.setScore(cursor.getFloat(cursor.getColumnIndex(DataContract.Results.RESULT_SCORE)));
                    toReturn.add(thisResult);
                }
            }
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return toReturn;
    }

    public static long addPhoto(Context context, Photo photo) {
        long photoId = INVALID_PHOTO_ID;
        try {
            Uri uri = DataContract.Photos.CONTENT_URI;
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataContract.Photos.PHOTO_DATE, photo.getDate());
            contentValues.put(DataContract.Photos.PHOTO_PATH, photo.getPhotoPath());

            uri = context.getContentResolver().insert(uri, contentValues);
            Log.d(TAG, "INSERTED photo " + photo.getPhotoPath());

            photoId = ContentUris.parseId(uri);
            for (Result result : photo.getResults()) {
                addResult(context, result, photoId);
            }

        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        }
        return photoId;
    }

    private static boolean addResult(Context context, Result result, long photoId) {
        boolean success = false;
        Cursor cursor = null;
        try {
            Uri uri = DataContract.Results.CONTENT_URI;
            ContentValues contentValues = new ContentValues();
            contentValues.put(DataContract.Results.PHOTO_ID, photoId);
            contentValues.put(DataContract.Results.RESULT_LABEL, result.getLabel());
            contentValues.put(DataContract.Results.RESULT_SCORE, result.getScore());

            context.getContentResolver().insert(uri, contentValues);
            Log.d(TAG, "INSERTED result " + result.getLabel() + " for photo " + photoId);
            success = true;
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return success;
    }

    public static long deletePhoto(Context context, Photo photo) {
        long photoId = INVALID_PHOTO_ID;
        try {
            Uri uri = ContentUris.withAppendedId(DataContract.Photos.CONTENT_URI, photo.getId());

            int deleted = context.getContentResolver().delete(uri, null, null);
            if (deleted > 0) {
                Log.d(TAG, "deleted " + deleted + " photo with id " + photo.getId());
                deleteResults(context, photo.getId());
            }
        } catch (SQLiteException e) {
            Log.e(TAG, e.getMessage());
        } catch (IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        }
        return photoId;
    }

    private static void deleteResults(Context context, long id) {
        Cursor cursor = null;
        try {
            Uri uri = DataContract.Results.CONTENT_URI;

            String selection = DataContract.Results.PHOTO_ID + " = ?";
            String[] selectionArgs = {String.valueOf(id)};

            int deleted = context.getContentResolver().delete(uri, selection, selectionArgs);
            if (deleted > 0) {
                Log.d(TAG, "deleted " + deleted + " result for photo with id " + id);
            }
        } catch (SQLiteException | IllegalArgumentException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    private void clearDatabase(Context context) {
        context.getContentResolver().delete(DataContract.Photos.CONTENT_URI, null, null);
    }


    public static void getPhotosIntent(Context context) {
        Intent intent = new Intent(context, DatabaseDAL.class);
        intent.setAction(DatabaseDAL.GET_PHOTOS);
        context.startService(intent);
    }

    public static void addPhotoIntent(Context context, Photo photo) {
        Intent intent = new Intent(context, DatabaseDAL.class);
        intent.putExtra(KEY_PHOTO, photo);
        intent.setAction(DatabaseDAL.ADD_PHOTO);
        context.startService(intent);
    }

    public static void clearDatabaseIntent(Context context) {
        Intent intent = new Intent(context, DatabaseDAL.class);
        intent.setAction(DatabaseDAL.CLEAR_DATABASE);
        context.startService(intent);
    }

    public static void deletePhotoIntent(Context context, Photo photo) {
        Intent intent = new Intent(context, DatabaseDAL.class);
        intent.setAction(DatabaseDAL.DELETE_PHOTO);
        intent.putExtra(KEY_PHOTO, photo);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(TAG, "Intent received " + intent.getAction());
        String action = intent.getAction();
        switch (action) {
            case GET_PHOTOS:
                getPhotos(this);
                return;
            case GET_RESULTS:
                getResults(this, intent.getLongExtra(KEY_PHOTO_ID, 0));
                return;
            case ADD_PHOTO:
                addPhoto(this, (Photo) intent.getSerializableExtra(KEY_PHOTO));
                return;
            case DELETE_PHOTO:
                deletePhoto(this, (Photo) intent.getSerializableExtra(KEY_PHOTO));
                return;
            case CLEAR_DATABASE:
                clearDatabase(this);
                return;
            default:
        }
    }

    public DatabaseDAL() {
        super("DatabaseDAL");
    }
}
