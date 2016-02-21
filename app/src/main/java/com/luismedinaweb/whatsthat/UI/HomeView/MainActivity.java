package com.luismedinaweb.whatsthat.UI.HomeView;

import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
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
import com.luismedinaweb.whatsthat.Data.contentprovider.DataContract;
import com.luismedinaweb.whatsthat.Data.contentprovider.DatabaseDAL;
import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.R;
import com.luismedinaweb.whatsthat.UI.AbstractActivity;
import com.luismedinaweb.whatsthat.UI.ProcessingView.ProcessingActivity;
import com.luismedinaweb.whatsthat.UI.ProcessingView.TaskProcessFragment;
import com.luismedinaweb.whatsthat.UI.ResultView.ResultFragment;
import com.luismedinaweb.whatsthat.Utility;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MainActivity extends AbstractActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();
    public static final String KEY_PROCESSING_RESULT = "key_result";
    //private static final String KEY_EDITING = "key_editing";
    private View mEmptyView;
    //private View mHistoryLayout;
    private PhotosAdapter mAdapter;
    private RecyclerView mRecyclerView;
    private TaskProcessFragment mTaskFragment;
    private Toolbar mEditingToolbar;
    private Menu mMenu;
    private boolean mEditing;
    private long mSelectedId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);

//        if(savedInstanceState != null){
//            mEditing = savedInstanceState.getBoolean(KEY_EDITING);
//        }

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

    }

    private void initAdView() {
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device_id))
                .build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == AbstractActivity.REQUEST_RETURN_RESULT && resultCode == RESULT_OK) {
            mSelectedId = data.getLongExtra(ProcessingActivity.KEY_PHOTO_ID, -1);
            for (Photo photo : mAdapter.getPhotos()) {
                if (photo.getId() == mSelectedId) {
                    Log.e("MAIN", "Found photo to select with id " + mSelectedId);
                    break;
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        mMenu = menu;
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
            deleteFiles(mAdapter.getPhotos());
            deselectSelectedItem();
            return true;
        } else if (id == R.id.action_clear_history) {
            DatabaseDAL.clearDatabaseIntent(this);
            deselectSelectedItem();
            return true;
        } else if (id == R.id.action_edit_delete) {
            deleteFiles(mAdapter.getSelected());
            deselectSelectedItem();
            return true;
        } else if (id == R.id.action_edit_clear) {
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
            cursor.moveToPosition(-1);  //Reset the cursor in case we already looped through it.
            while (cursor.moveToNext()) {
                Photo thisPhoto = new Photo();
                thisPhoto.setId(cursor.getInt(cursor.getColumnIndex(DataContract.Photos._ID)));
                thisPhoto.setDate(cursor.getLong(cursor.getColumnIndex(DataContract.Photos.PHOTO_DATE)));
                thisPhoto.setPhotoPath(cursor.getString(cursor.getColumnIndex(DataContract.Photos.PHOTO_PATH)));
                photos.add(thisPhoto);
            }
            mAdapter.refresh(photos);
            setEmptyView(photos.isEmpty());
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
            private TextView mPhotoPathTextView;
            private TextView mPhotoDateTextView;
            private CheckBox mSelectCheckbox;

            public PhotosViewHolder(View itemView) {
                super(itemView);

                mPhotoImageView = (ImageView) itemView.findViewById(R.id.photo_imageView);
                mPhotoPathTextView = (TextView) itemView.findViewById(R.id.path_textView);
                mPhotoDateTextView = (TextView) itemView.findViewById(R.id.date_textView);
                mSelectCheckbox = (CheckBox) itemView.findViewById(R.id.select_checkBox);

            }

            private void doClick(Photo photo, int position) {
                if (Utility.TWO_PANE) {
                    getFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container,
                                    ResultFragment.newInstance(photo))
                            .commit();
                } else {
                    goToResults(photo);
                }
                mSelectedId = photo.getId();
                notifyItemChanged(mPreviouslySelectedRow);
                itemView.setSelected(true);
                mPreviouslySelectedRow = position;
            }

            public void initialize(final Photo photo, final int position) {
                Uri uri = Uri.parse("file:" + photo.getPhotoPath());

                Picasso.with(itemView.getContext())
                        .load(uri)
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .resize(mPhotoImageView.getLayoutParams().width, mPhotoImageView.getLayoutParams().height)
                        .into(mPhotoImageView);

                String fileName = uri.getLastPathSegment();
                mPhotoPathTextView.setText(fileName);

                Date date = Utility.getDateFromFileName(fileName);
                SimpleDateFormat sdf = new SimpleDateFormat("M/dd/yyyy h:mm:ss a");
                mPhotoDateTextView.setText(sdf.format(date));

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        doClick(photo, position);
                    }
                });

                if (mSelectedId == photo.getId()) {
                    if (Utility.TWO_PANE) {
                        getFragmentManager().beginTransaction()
                                .replace(R.id.fragment_container,
                                        ResultFragment.newInstance(photo))
                                .commit();
                    }
                    itemView.setSelected(true);
                    mPreviouslySelectedRow = position;
                } else {
                    itemView.setSelected(false);
                }

                itemView.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        mEditing = true;
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
