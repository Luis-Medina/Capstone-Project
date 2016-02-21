package com.luismedinaweb.whatsthat.Data.contentprovider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Luis on 2/3/2015.
 */
public class DataContract {

    interface PhotosColumns {
        String PHOTO_PATH = "path";
        String PHOTO_DATE = "date";
    }

    interface ResultsColumns {
        String PHOTO_ID = "photo_id";
        String RESULT_LABEL = "label";
        String RESULT_SCORE = "score";
    }


    private static final String PATH_PHOTOS = "photos";
    private static final String PATH_RESULTS = "results";

    public static final String CONTENT_AUTHORITY = "com.luismedinaweb.whatsthat.provider";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);


    public static class DefaultTableClass implements BaseColumns {

        public static final int CONTENT_TYPE = 0;
        public static final int CONTENT_ITEM_TYPE = 1;

        public static String getTableUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static Uri getContentUri(String tableName) {
            return BASE_CONTENT_URI.buildUpon().appendEncodedPath(tableName).build();
        }

        public static Uri buildTableUri(String tableName, String vendorId) {
            Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendEncodedPath(tableName).build();
            return CONTENT_URI.buildUpon().appendEncodedPath(vendorId).build();
        }

        public static String getContentType(String tableName, int contentTypeType) {
            if (contentTypeType == CONTENT_TYPE) {
                return "vnd.android.cursor.dir/vnd." + CONTENT_AUTHORITY + "." + tableName;
            }
            if (contentTypeType == CONTENT_ITEM_TYPE) {
                return "vnd.android.cursor.item/vnd." + CONTENT_AUTHORITY + "." + tableName;
            }
            throw new IllegalArgumentException("Unknown content type");
        }

    }

    public static class Photos extends DefaultTableClass implements PhotosColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_PHOTOS).build();
        public static final Uri CONTENT_URI_WITH_ID =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_PHOTOS).build();
    }

    public static class Results extends DefaultTableClass implements ResultsColumns, BaseColumns {
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendEncodedPath(PATH_RESULTS).build();
    }


}
