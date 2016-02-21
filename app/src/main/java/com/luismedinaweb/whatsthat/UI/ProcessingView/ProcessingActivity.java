package com.luismedinaweb.whatsthat.UI.ProcessingView;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.R;
import com.luismedinaweb.whatsthat.UI.AbstractActivity;
import com.luismedinaweb.whatsthat.Utility;

/**
 * Created by Luis on 2/15/2016.
 */
public class ProcessingActivity extends AbstractActivity implements TaskProcessFragment.TaskCallbacks {

    private String mPhotoPath;
    public static final String KEY_PHOTO_PATH = "key_photo_path";
    public static final String KEY_PHOTO_ID = "key_photo_id";
    private static final String TAG_TASK_FRAGMENT = "task_fragment";
    private TaskProcessFragment mTaskFragment;
    private AlertDialog mDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.processing_view);

        if (getIntent() != null) {
            if (getSupportActionBar() != null) {
                getSupportActionBar().setTitle(R.string.actionbar_title_processing);
            }

            mPhotoPath = getIntent().getStringExtra(KEY_PHOTO_PATH);

            if (mPhotoPath != null && !mPhotoPath.isEmpty()) {
                FragmentManager fm = getFragmentManager();
                mTaskFragment = (TaskProcessFragment) fm.findFragmentByTag(TAG_TASK_FRAGMENT);

                // If the Fragment is non-null, then it is currently being
                // retained across a configuration change.
                if (mTaskFragment == null) {
                    mTaskFragment = new TaskProcessFragment();
                    fm.beginTransaction().add(mTaskFragment, TAG_TASK_FRAGMENT).commit();
                    mTaskFragment.start(mPhotoPath);
                }
            } else {
                finish();
            }

        } else {
            finish();
        }
    }


    @Override
    public void onCancelled() {
        Toast.makeText(this, "Task cancelled!", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public void onPostExecute(Photo photo) {
        if (Utility.TWO_PANE) {
            if (photo == null) {
                Toast.makeText(this, "Unable to process photo. Please try again.", Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent();
                intent.putExtra(KEY_PHOTO_ID, photo == null ? -1 : photo.getId());
                setResult(RESULT_OK, intent);
            }
            finish();
        } else {
            goToResults(photo);
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        if (mDialog == null) {
            createDialog();
        }
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
    }

    private void createDialog() {
        mDialog = new AlertDialog.Builder(this)
                .setTitle("Are you sure you want to cancel this task?")
                .setCancelable(true)
                .setPositiveButton("Yes",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                finish();
                            }
                        }
                )
                .setNegativeButton("No",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                dialog.dismiss();
                            }
                        }
                )
                .create();
    }
}
