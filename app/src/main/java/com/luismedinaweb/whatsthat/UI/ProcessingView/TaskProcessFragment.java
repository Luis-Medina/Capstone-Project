package com.luismedinaweb.whatsthat.UI.ProcessingView;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.luismedinaweb.whatsthat.Data.contentprovider.DatabaseDAL;
import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.Data.model.base.Result;
import com.luismedinaweb.whatsthat.Data.model.visionapi.AnnotateImageRequest;
import com.luismedinaweb.whatsthat.Data.model.visionapi.AnnotateImageResponse;
import com.luismedinaweb.whatsthat.Data.model.visionapi.BatchImageRequest;
import com.luismedinaweb.whatsthat.Data.model.visionapi.BatchImageResponse;
import com.luismedinaweb.whatsthat.Data.model.visionapi.EntityAnnotation;
import com.luismedinaweb.whatsthat.Data.model.visionapi.Feature;
import com.luismedinaweb.whatsthat.Data.model.visionapi.Image;
import com.luismedinaweb.whatsthat.R;
import com.luismedinaweb.whatsthat.Utility;
import com.squareup.okhttp.MediaType;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.RequestBody;
import com.squareup.okhttp.Response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by lmedina on 2/17/2016.
 */
public class TaskProcessFragment extends Fragment {

    private static final String LOG_TAG = TaskProcessFragment.class.getSimpleName();
    private TaskCallbacks mCallbacks;
    private ProcessImageTask mTask;
    private boolean mTaskInProgress;
    private Context mContext;
    private String API_KEY = "";
    private String ENDPOINT_URL = "https://vision.googleapis.com/v1/images:annotate?key=";


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mCallbacks = (TaskCallbacks) context;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        mCallbacks = (TaskCallbacks) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Retain this fragment across configuration changes.
        setRetainInstance(true);

        API_KEY = getString(R.string.API_KEY);

        if (API_KEY.isEmpty()) {
            Toast.makeText(getActivity(), "Invalid API KEY for Google Vision API. Place your key in API_KEY on strings.xml", Toast.LENGTH_LONG).show();
        }

    }

    public void start(String photoPath) {
        // Create and execute the background task.
        if (!mTaskInProgress) {
            mTask = new ProcessImageTask();
            mTask.execute(photoPath);
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mContext = null;
        mCallbacks = null;
    }


    private class ProcessImageTask extends AsyncTask<String, Void, Photo> {

        private String encodedString;
        private String mPhotoPath = "";
        public static final long INVALID_PHOTO_ID = DatabaseDAL.INVALID_PHOTO_ID;


        @Override
        protected Photo doInBackground(String... params) {
            Photo photo = null;
            if (params[0] != null) {
                mPhotoPath = params[0];
                photo = send(mPhotoPath);
            }
            return photo;
        }

        private Photo send(String path) {
            final OkHttpClient client = new OkHttpClient();
            String url = ENDPOINT_URL + API_KEY;
            final MediaType MEDIA_TYPE = MediaType.parse("application/json; charset=utf-8");

            BatchImageRequest batchImageRequest = new BatchImageRequest();
            ArrayList<AnnotateImageRequest> requests = new ArrayList<>();
            AnnotateImageRequest newRequest = new AnnotateImageRequest();

            Image image = new Image();
            if (path != null) {
                byte[] imageBytes = resizeBitmap(path);
                encodedString = Base64.encodeToString(imageBytes, Base64.DEFAULT);
                image.setContent(encodedString);
                newRequest.setImage(image);
            } else {
                Log.e(LOG_TAG, "File is null");
                return null;
            }

            ArrayList<Feature> features = new ArrayList<>();
            Feature feature = new Feature();
            feature.setMaxResults(10);
            feature.setType(Feature.Type.LABEL_DETECTION);
            features.add(feature);
            newRequest.setFeature(features);

            requests.add(newRequest);
            batchImageRequest.setRequests(requests);

            String jsonString = new Gson().toJson(batchImageRequest);

            final Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MEDIA_TYPE, jsonString))
                    .build();

            try {
                Response response = client.newCall(request).execute();
                if (response != null) {
                    String responseBody = response.body().string();
                    JsonElement element;
                    try {
                        element = new JsonParser().parse(responseBody);
                        Gson gson = new Gson();
                        final BatchImageResponse authenticationResponse = gson.fromJson(element, BatchImageResponse.class);
                        if (authenticationResponse.getResponses().size() > 0) {
                            AnnotateImageResponse imageResponse = authenticationResponse.getResponses().get(0);
                            return addPhotoToDatabase(imageResponse.getLabelAnnotations());
                        } else {
                            Log.e(LOG_TAG, "Received no responses!");
                        }
                    } catch (JsonSyntaxException ex) {
                        Log.e(LOG_TAG, ex.getMessage());
                    }
                } else {
                    Log.e(LOG_TAG, "Got null response from server");
                }

            } catch (IOException e) {
                Log.e(LOG_TAG, e.getMessage());
                e.printStackTrace();
            }
            return null;
        }


        private Photo addPhotoToDatabase(ArrayList<EntityAnnotation> results) {
            long photoId = INVALID_PHOTO_ID;

            Uri uri = Uri.parse("file:" + mPhotoPath);

            String fileName = uri.getLastPathSegment();
            Date date = Utility.getDateFromFileName(fileName);
            Photo photo = new Photo(mPhotoPath, date.getTime());

            if (results != null) {
                for (EntityAnnotation annotation : results) {
                    photo.addResult(new Result(annotation.getDescription(), annotation.getScore()));
                }
            }

            if (mContext != null) {
                photoId = DatabaseDAL.addPhoto(mContext, photo);
            }

            photo.setId(photoId);

            return photo;
        }

        private byte[] resizeBitmap(String photoPath) {
            Bitmap toUse;
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = false;
            toUse = BitmapFactory.decodeFile(photoPath, bmOptions);

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            int targetW = 300;
            int targetH = 300;

            bmOptions.inSampleSize = Math.min(photoW / targetW, photoH / targetH);
            bmOptions.inPurgeable = true;

            toUse = BitmapFactory.decodeFile(photoPath, bmOptions);

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            toUse.compress(Bitmap.CompressFormat.JPEG, 100, stream);

            return stream.toByteArray();

        }

        @Override
        protected void onCancelled() {
            if (mCallbacks != null) {
                mCallbacks.onCancelled();
            }
        }

        @Override
        protected void onPostExecute(Photo photo) {
            if (mCallbacks != null) {
                mCallbacks.onPostExecute(photo);
            }
        }
    }


    interface TaskCallbacks {
        void onCancelled();

        void onPostExecute(Photo photo);
    }

}
