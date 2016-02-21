package com.luismedinaweb.whatsthat.UI.ResultView;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.luismedinaweb.whatsthat.Data.contentprovider.DataContract;
import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.Data.model.base.Result;
import com.luismedinaweb.whatsthat.R;
import com.squareup.picasso.Picasso;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by Luis on 2/15/2016.
 */
public class ResultFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public static final String KEY_PHOTO = "key_photo";
    //private String mPhotoPath;
    private ArrayList<Result> mResults = new ArrayList<>();
    private ImageView mImageView;
    private RecyclerView mRecyclerView;
    //private long mPhotoId;
    private ResultsAdapter mAdapter;
    private AdView mAdView;
    private AdRequest mAdRequest;
    private View mEmptyView;
    private View mResultView;
    private Photo mPhoto;

    public ResultFragment() {
    }


    public static ResultFragment newInstance() {
        ResultFragment fragment = new ResultFragment();

        return fragment;
    }

    public static ResultFragment newInstance(Photo photo) {
        ResultFragment fragment = new ResultFragment();

        Bundle args = new Bundle();
        args.putSerializable(KEY_PHOTO, photo);
        fragment.setArguments(args);

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRetainInstance(true);

        if (getArguments() != null) {

            Object temp = getArguments().getSerializable(KEY_PHOTO);
            if (temp != null && temp instanceof Photo) {
                mPhoto = (Photo) temp;
            }
//            Object results = getArguments().getSerializable(KEY_RESULTS);
//            if(results != null){
//                mResults.addAll((ArrayList<EntityAnnotation>) results);
//            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.result_fragment, container, false);

        mEmptyView = view.findViewById(R.id.emptyView);
        mResultView = view.findViewById(R.id.resultViewLayout);
        mImageView = (ImageView) view.findViewById(R.id.imageView);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mAdapter = new ResultsAdapter(new ArrayList<Result>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mRecyclerView.setAdapter(mAdapter);


        if (mPhoto == null) {
            mResultView.setVisibility(View.GONE);
            mEmptyView.setVisibility(View.VISIBLE);
        } else {
            mResultView.setVisibility(View.VISIBLE);
            mEmptyView.setVisibility(View.GONE);

            view.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
                @Override
                public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                    view.removeOnLayoutChangeListener(this);
                    Picasso.with(getActivity())
                            .load("file:///" + mPhoto.getPhotoPath())
                            .placeholder(android.R.drawable.ic_menu_gallery)
                            .centerCrop()
                            .resize(mImageView.getWidth(), mImageView.getHeight())
                            .into(mImageView);
                }
            });
        }

        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        if (mPhoto != null) getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = DataContract.Results.CONTENT_URI;
        String[] projection = new String[]{
                DataContract.Results.PHOTO_ID,
                DataContract.Results.RESULT_LABEL,
                DataContract.Results.RESULT_SCORE
        };

        String selection = DataContract.Results.PHOTO_ID + " = ? ";
        String[] selectionArgs = {String.valueOf(mPhoto.getId())};

        String sortOrder = DataContract.Results.RESULT_SCORE + " DESC";

        return new CursorLoader(getActivity(), uri, projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor != null) {
            ArrayList<Result> results = new ArrayList<>();
            while (cursor.moveToNext()) {
                Result thisResult = new Result();
                thisResult.setPhotoId(cursor.getInt(cursor.getColumnIndex(DataContract.Results.PHOTO_ID)));
                thisResult.setLabel(cursor.getString(cursor.getColumnIndex(DataContract.Results.RESULT_LABEL)));
                thisResult.setScore(cursor.getFloat(cursor.getColumnIndex(DataContract.Results.RESULT_SCORE)));
                results.add(thisResult);
            }
            mAdapter.refresh(results);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.clear();
    }


    public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.AbstractViewHolder> {

        private ArrayList<Result> mResults;
        private static final int HEADER_VIEW_TYPE = 1;
        private static final int ITEM_VIEW_TYPE = 0;

        public ResultsAdapter(ArrayList<Result> results) {
            if (mResults == null) {
                mResults = new ArrayList<>();
            }

            if (results != null) {
                mResults.addAll(results);
                if (!mResults.isEmpty()) mResults.add(1, new Result());

            }
        }

        public void refresh(ArrayList<Result> results) {
            mResults.clear();
            if (results != null) {
                mResults.addAll(results);
                if (!mResults.isEmpty()) mResults.add(1, new Result());
            }

            notifyDataSetChanged();
        }

        public void clear() {
            mResults.clear();
            notifyDataSetChanged();
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 1) {
                return HEADER_VIEW_TYPE;
            } else {
                return ITEM_VIEW_TYPE;
            }
        }

        @Override
        public AbstractViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view;

            if (viewType == HEADER_VIEW_TYPE) {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list_item_header, parent, false);
                return new ResultsHeaderViewHolder(view);
            } else {
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.result_list_item_combined, parent, false);
                return new ResultsViewHolder(view);
            }


        }

        @Override
        public void onBindViewHolder(AbstractViewHolder holder, int position) {
            Result result = mResults.get(position);

            holder.initialize(result, position);

        }

        @Override
        public int getItemCount() {
            if (mResults == null) {
                return 0;
            }

            return mResults.size();
        }

        public abstract class AbstractViewHolder extends RecyclerView.ViewHolder {

            protected final TextView mResultLabelTextView;

            public AbstractViewHolder(View itemView) {
                super(itemView);

                mResultLabelTextView = (TextView) itemView.findViewById(R.id.result_text);
            }

            public abstract void initialize(Result result, int position);
        }


        public class ResultsViewHolder extends AbstractViewHolder {

            public ResultsViewHolder(View itemView) {
                super(itemView);
            }

            public void initialize(final Result result, int position) {
                NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
                nf.setMaximumFractionDigits(2);
                String score = nf.format(result.getScore());

                SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                stringBuilder.append("I'm ");
                ForegroundColorSpan colorSpan = new ForegroundColorSpan(ContextCompat.getColor(
                        itemView.getContext(), R.color.colorAccent));
                int start = stringBuilder.length();
                stringBuilder.append(score);
                stringBuilder.setSpan(colorSpan, start, start + score.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                stringBuilder.append(" sure this is: ");
                start = stringBuilder.length();
                stringBuilder.append(result.getLabel());
                ForegroundColorSpan colorSpan1 = new ForegroundColorSpan(ContextCompat.getColor(
                        itemView.getContext(), R.color.colorPrimary));
                stringBuilder.setSpan(colorSpan1, start, start + result.getLabel().length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                mResultLabelTextView.setText(stringBuilder);

                if (position == 0) {
                    mResultLabelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 22);
                    //mResultLabelTextView.setTextSize(getResources().getDimension(R.dimen.first_result_text_size));
                } else {
                    mResultLabelTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                    //mResultLabelTextView.setTextSize(getResources().getDimension(R.dimen.other_result_text_size));
                }

            }
        }

        public class ResultsHeaderViewHolder extends AbstractViewHolder {

            public ResultsHeaderViewHolder(View itemView) {
                super(itemView);
            }

            @Override
            public void initialize(Result result, int position) {
                if (getItemCount() == 0) {
                    mResultLabelTextView.setText("That's all I got!");
                } else {
                    mResultLabelTextView.setText("Here are some other guesses:");
                }
                mResultLabelTextView.setTypeface(Typeface.DEFAULT_BOLD);
            }
        }


        public class OldResultsViewHolder extends RecyclerView.ViewHolder {

            private TextView mResultLabelTextView;
            private TextView mResultScoreTextView;

            public OldResultsViewHolder(View itemView) {
                super(itemView);

                mResultLabelTextView = (TextView) itemView.findViewById(R.id.resultLabel);
                mResultScoreTextView = (TextView) itemView.findViewById(R.id.resultScore);
            }

            public void initialize(final Result result, int position) {
                mResultLabelTextView.setText(result.getLabel());
                NumberFormat nf = NumberFormat.getPercentInstance(Locale.getDefault());
                nf.setMaximumFractionDigits(2);
                mResultScoreTextView.setText(nf.format(result.getScore()));
            }
        }
    }

}
