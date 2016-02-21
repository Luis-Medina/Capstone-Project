package com.luismedinaweb.whatsthat.UI.ResultView;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Menu;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.luismedinaweb.whatsthat.Data.model.base.Photo;
import com.luismedinaweb.whatsthat.R;
import com.luismedinaweb.whatsthat.UI.AbstractActivity;

/**
 * Created by Luis on 2/15/2016.
 */
public class ResultActivity extends AbstractActivity {

    private AdView mAdView;
    private AdRequest mAdRequest;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.result_view);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.actionbar_title_results);
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        Photo photo = null;
        if (getIntent() != null) {
            Object temp = getIntent().getSerializableExtra(ResultFragment.KEY_PHOTO);
            if (temp != null && temp instanceof Photo) {
                photo = (Photo) temp;
            }
        }

        ResultFragment resultFragment = ResultFragment.newInstance(photo);

        getFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, resultFragment)
                .commit();

        mAdView = (AdView) findViewById(R.id.adView);
        mAdRequest = new AdRequest.Builder()
                .addTestDevice(getString(R.string.test_device_id))
                .build();
        if (mAdView != null) mAdView.loadAd(mAdRequest);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_result, menu);
        return true;
    }


}
