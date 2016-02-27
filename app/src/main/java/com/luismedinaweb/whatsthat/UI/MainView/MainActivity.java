package com.luismedinaweb.whatsthat.UI.MainView;

import android.Manifest;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.analytics.HitBuilders;
import com.luismedinaweb.whatsthat.Data.contentprovider.DataContract;
import com.luismedinaweb.whatsthat.Data.contentprovider.DatabaseDAL;
import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.R;
import com.luismedinaweb.whatsthat.UI.AbstractActivity;
import com.luismedinaweb.whatsthat.UI.ProcessingView.ProcessingActivity;
import com.luismedinaweb.whatsthat.UI.ResultView.ResultFragment;
import com.luismedinaweb.whatsthat.Utility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AbstractActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    private static final String KEY_SELECTED_ID = "key_selected_id";
    private static final String KEY_IS_EDITING = "key_editing";
    public static final String ACTION_TAKE_PHOTO = "action_take_photo";
    private View mEmptyView;
    private PhotosAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private boolean mEditing;
    private static final long DEFAULT_ID = -1;
    private long mSelectedId = DEFAULT_ID;
    private static final String[] mRequiredPermissions = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private static final int MY_PERMISSIONS_REQUEST = 11;
    private View mTutorialView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);

        if (savedInstanceState != null) {
            mSelectedId = savedInstanceState.getLong(KEY_SELECTED_ID, DEFAULT_ID);
            mEditing = savedInstanceState.getBoolean(KEY_IS_EDITING, false);
        }

        mTutorialView = findViewById(R.id.tutorialView);
        mTutorialView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mPreferences.edit().putBoolean(KEY_TUTORIAL_SHOWN, true).apply();
                mTutorialView.setVisibility(View.GONE);
            }
        });

        mEmptyView = findViewById(R.id.emptyView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView);

        mAdapter = new PhotosAdapter(new ArrayList<Photo>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(mAdapter);

        setEmptyView(true);
        getLoaderManager().initLoader(0, null, this);

        if (findViewById(R.id.fragment_container) != null) {
            Utility.TWO_PANE = true;
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ResultFragment.newInstance())
                    .commit();
        }

        initAdView();

        checkPermissions();

        Intent receivedIntent = getIntent();
        if (receivedIntent != null && receivedIntent.getAction() != null) {
            if (receivedIntent.getAction().equals(ACTION_TAKE_PHOTO)) {
                takePhoto();
            }
        }

    }

    private void checkPermissions() {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        for (String permission : mRequiredPermissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(permission);
            }
        }

        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[permissionsToRequest.size()]),
                    MY_PERMISSIONS_REQUEST);
        }
    }

    private void initAdView() {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device_id))
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (intent != null) {
            if (intent.getAction().equals(ACTION_TAKE_PHOTO)) {
                takePhoto();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AbstractActivity.REQUEST_RETURN_RESULT && resultCode == RESULT_OK) {
            mSelectedId = data.getLongExtra(ProcessingActivity.KEY_PHOTO_ID, -1);
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        if (mEditing) {
            getMenuInflater().inflate(R.menu.menu_edit, menu);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle("");
            if (mAdapter != null) mAdapter.notifyDataSetChanged();
        } else {
            getMenuInflater().inflate(R.menu.menu_main, menu);
            if (getSupportActionBar() != null) getSupportActionBar().setTitle(R.string.app_name);
        }

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_delete_files) {
            sendEvent("Delete all files");
            deleteFiles(mAdapter.getPhotos());
            deselectSelectedItem();
            return true;
        } else if (id == R.id.action_clear_history) {
            sendEvent("Clear all files");
            DatabaseDAL.clearDatabaseIntent(this);
            deselectSelectedItem();
            return true;
        } else if (id == R.id.action_edit_delete) {
            sendEvent("Delete selected files");
            deleteFiles(mAdapter.getSelected());
            deselectSelectedItem();
            return true;
        } else if (id == R.id.action_edit_clear) {
            sendEvent("Clear selected files");
            deselectSelectedItem();
            for (Photo photo : mAdapter.getSelected()) {
                DatabaseDAL.deletePhotoIntent(this, photo);
            }
        } else if (id == R.id.action_edit_done) {
            finishEditing();
            return true;
        } else if (id == R.id.action_edit_select) {
            if (item.getTitle().equals(getString(R.string.action_short_all))) {
                mAdapter.selectAll();
                item.setTitle(R.string.action_short_none);
            } else {
                mAdapter.clearSelected();
                item.setTitle(R.string.action_short_all);
            }
            mAdapter.notifyDataSetChanged();
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendEvent(String action) {
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory("Action")
                .setAction(action)
                .build());
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putLong(KEY_SELECTED_ID, mSelectedId);
        outState.putBoolean(KEY_IS_EDITING, mEditing);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!Utility.TWO_PANE) {
            mSelectedId = DEFAULT_ID;
        }
        if (!mPreferences.getBoolean(KEY_TUTORIAL_SHOWN, false)) {
            mTutorialView.setVisibility(View.VISIBLE);
        } else {
            mTutorialView.setVisibility(View.GONE);
        }
    }

    private void finishEditing() {
        mEditing = false;
        mAdapter.clearSelected();
        mAdapter.notifyDataSetChanged();
        invalidateOptionsMenu();
    }

    private void deselectSelectedItem() {
        if (Utility.TWO_PANE) {
            getFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, ResultFragment.newInstance())
                    .commit();
        }
    }

    private void deleteFiles(ArrayList<Photo> photos) {
        for (Photo photo : photos) {
            File file = new File(photo.getPhotoPath());
            boolean deleted = file.delete();
            if (!deleted) {
                Log.e(LOG_TAG, "Unable to delete file " + file.getAbsolutePath());
            }
            DatabaseDAL.deletePhotoIntent(this, photo);
        }
    }

    private void setEmptyView(boolean empty) {
        if (empty) {
            mEmptyView.setVisibility(View.VISIBLE);
            if (mEditing) {
                finishEditing();
            }
        } else {
            mEmptyView.setVisibility(View.GONE);
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = DataContract.Photos.CONTENT_URI;
        String[] projection = new String[]{
                DataContract.Photos._ID,
                DataContract.Photos.PHOTO_DATE,
                DataContract.Photos.PHOTO_PATH
        };

        return new CursorLoader(this, uri, projection, null, null, DataContract.Photos.PHOTO_DATE + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            ArrayList<Photo> photos = new ArrayList<>();
            int index = 0;
            int scrollPosition = 0;
            cursor.moveToPosition(-1);  //Reset the cursor in case we already looped through it.
            while (cursor.moveToNext()) {
                Photo thisPhoto = new Photo();
                thisPhoto.setId(cursor.getInt(cursor.getColumnIndex(DataContract.Photos._ID)));
                thisPhoto.setDate(cursor.getLong(cursor.getColumnIndex(DataContract.Photos.PHOTO_DATE)));
                thisPhoto.setPhotoPath(cursor.getString(cursor.getColumnIndex(DataContract.Photos.PHOTO_PATH)));
                photos.add(thisPhoto);

                if (thisPhoto.getId() == mSelectedId) {
                    scrollPosition = index;
                }

                index++;
            }
            mAdapter.refresh(photos);
            setEmptyView(photos.isEmpty());
            mRecyclerView.smoothScrollToPosition(scrollPosition);

        } else {
            setEmptyView(true);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.clear();
        setEmptyView(true);
    }


    public class PhotosAdapter extends RecyclerView.Adapter<PhotosAdapter.PhotosViewHolder> {

        private ArrayList<Photo> mPhotos;
        private boolean[] mSelected;
        private int mPreviouslySelectedRow = -1;

        public PhotosAdapter(ArrayList<Photo> photos) {
            if (mPhotos == null) {
                mPhotos = new ArrayList<>();
            }

            if (photos != null) {
                mPhotos.addAll(photos);
                mSelected = new boolean[photos.size()];
            }
        }

        @Override
        public PhotosViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photos_list_item, parent, false);

            return new PhotosViewHolder(view);
        }

        public ArrayList<Photo> getPhotos() {
            return mPhotos;
        }

        @Override
        public void onBindViewHolder(PhotosViewHolder holder, int position) {
            Photo photo = mPhotos.get(position);

            holder.initialize(photo, position);
        }

        @Override
        public int getItemCount() {
            if (mPhotos == null) {
                return 0;
            }

            return mPhotos.size();
        }

        public void refresh(ArrayList<Photo> photos) {
            mPhotos.clear();
            if (photos != null) {
                mPhotos.addAll(photos);
                mSelected = new boolean[photos.size()];
            }

            notifyDataSetChanged();
        }

        public void clear() {
            mPhotos.clear();
            notifyDataSetChanged();
        }

        public ArrayList<Photo> getSelected() {
            ArrayList<Photo> selected = new ArrayList<>();
            for (int i = 0; i < mPhotos.size(); i++) {
                if (mSelected[i]) {
                    selected.add(mPhotos.get(i));
                }
            }
            return selected;
        }

        public void clearSelected() {
            for (int i = 0; i < mSelected.length; i++) {
                mSelected[i] = false;
            }
        }

        public void selectAll() {
            for (int i = 0; i < mSelected.length; i++) {
                mSelected[i] = true;
            }
        }


        public class PhotosViewHolder extends RecyclerView.ViewHolder {

            private ImageView mPhotoImageView;
            private TextView mPhotoDateTextView;
            private CheckBox mSelectCheckbox;
            private SimpleDateFormat mSdf = new SimpleDateFormat("M/dd/yyyy h:mm:ss a");

            public PhotosViewHolder(View itemView) {
                super(itemView);

                mPhotoImageView = (ImageView) itemView.findViewById(R.id.photo_imageView);
                mPhotoDateTextView = (TextView) itemView.findViewById(R.id.date_textView);
                mSelectCheckbox = (CheckBox) itemView.findViewById(R.id.select_checkBox);

            }

            private void doClick(Photo photo, int position) {
                if (Utility.TWO_PANE) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,
                                    ResultFragment.newInstance(photo))
                            .commit();

                    mSelectedId = photo.getId();
                    notifyItemChanged(mPreviouslySelectedRow);
                    itemView.setSelected(true);
                    mPreviouslySelectedRow = position;

                } else {
                    mSelectedId = photo.getId();
                    notifyItemChanged(mPreviouslySelectedRow);
                    itemView.setSelected(true);
                    mPreviouslySelectedRow = position;
                    goToResults(photo);
                }
            }

            public void initialize(final Photo photo, final int position) {
                Uri uri = Uri.parse("file:" + photo.getPhotoPath());

                Picasso.with(itemView.getContext())
                        .load(uri)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .fit()
                        .centerCrop()
                        .into(mPhotoImageView);

                String fileName = uri.getLastPathSegment();
                Date date = Utility.getDateFromFileName(fileName);
                mPhotoDateTextView.setText(mSdf.format(date));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doClick(photo, position);
                    }
                });

                if (Utility.TWO_PANE) {
                    if (mSelectedId == photo.getId()) {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,
                                        ResultFragment.newInstance(photo))
                                .commit();
                        itemView.setSelected(true);
                        mPreviouslySelectedRow = position;
                    } else {
                        itemView.setSelected(false);
                    }
                } else {
                    itemView.setSelected(false);
                }

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mEditing = true;
                        mSelectedId = photo.getId();
                        invalidateOptionsMenu();
                        return true;
                    }
                });

                if (mEditing) {
                    mSelectCheckbox.setVisibility(View.VISIBLE);
                } else {
                    mSelectCheckbox.setVisibility(View.GONE);
                }

                if (mSelected.length > 0) mSelectCheckbox.setChecked(mSelected[position]);

                mSelectCheckbox.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mSelected[position] = mSelectCheckbox.isChecked();
                    }
                });

            }

        }
    }


}
