package com.luismedinaweb.whatsthat.UI;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.luismedinaweb.whatsthat.AnalyticsApplication;
import com.luismedinaweb.whatsthat.Data.contentprovider.DatabaseDAL;
import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.R;
import com.luismedinaweb.whatsthat.UI.ProcessingView.ProcessingActivity;
import com.luismedinaweb.whatsthat.UI.ResultView.ResultActivity;
import com.luismedinaweb.whatsthat.UI.ResultView.ResultFragment;
import com.luismedinaweb.whatsthat.Utility;

import java.io.File;
import java.io.IOException;

/**
 * Created by lmedina on 2/17/2016.
 */
public abstract class AbstractActivity extends AppCompatActivity {

    private static final String KEY_PHOTO_PATH = "key_photo_path";
    private String mCurrentPhotoPath;
    private static final int REQUEST_TAKE_PHOTO = 1;
    public static final int REQUEST_RETURN_RESULT = 2;
    protected SharedPreferences mPreferences;
    protected static final String KEY_TUTORIAL_SHOWN = "key_tutorial_shown";
    protected Tracker mTracker;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (savedInstanceState != null) {
            mCurrentPhotoPath = savedInstanceState.getString(KEY_PHOTO_PATH, "");
        }

        // Obtain the shared Tracker instance.
        AnalyticsApplication application = (AnalyticsApplication) getApplication();
        mTracker = application.getDefaultTracker();

        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
    }

    protected void goToResults(Photo photo) {
        if (!Utility.TWO_PANE) {
            Intent intent = new Intent(this, ResultActivity.class);
            intent.putExtra(ResultFragment.KEY_PHOTO, photo);
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mTracker.setScreenName("ScreenView: " + getClass().getSimpleName());
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_camera) {
            mPreferences.edit().putBoolean(KEY_TUTORIAL_SHOWN, true).apply();
            takePhoto();
        } else if (id == R.id.action_clear_history) {
            DatabaseDAL.clearDatabaseIntent(this);
        }

        return super.onOptionsItemSelected(item);
    }

    public void takePhoto() {
        mCurrentPhotoPath = dispatchTakePictureIntent(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_TAKE_PHOTO && resultCode == RESULT_OK) {
            Intent intent = new Intent(this, ProcessingActivity.class);
            intent.putExtra(ProcessingActivity.KEY_PHOTO_PATH, mCurrentPhotoPath);
            if (Utility.TWO_PANE) {
                startActivityForResult(intent, REQUEST_RETURN_RESULT);
            } else {
                startActivity(intent);
            }
        }
    }


    private String dispatchTakePictureIntent(Activity activity) {
        // Save a file: path for use with ACTION_VIEW intents
        String photoPath = null;
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(activity.getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
                photoPath = photoFile.getAbsolutePath();
            } catch (IOException ex) {
                // Error occurred while creating the File
                Toast.makeText(activity, "Cannot save the photo.", Toast.LENGTH_SHORT).show();
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        Uri.fromFile(photoFile));
                activity.startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
        return photoPath;
    }


    private static File createImageFile() throws IOException {
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);

        File result;
        do {
            result = new File(storageDir, Utility.createFileName());
        } while (!result.createNewFile());
        return result;

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString(KEY_PHOTO_PATH, mCurrentPhotoPath);
        super.onSaveInstanceState(outState);
    }

}
