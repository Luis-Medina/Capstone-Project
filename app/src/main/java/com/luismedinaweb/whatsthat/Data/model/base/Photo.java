package com.luismedinaweb.whatsthat.Data.model.base;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by lmedina on 2/16/2016.
 */
public class Photo implements Serializable {

    private static final long serialVersionUID = 1L;
    private long mId = 0;
    private String mPhotoPath = "";
    private long mDate = 0;
    private ArrayList<Result> mResults = new ArrayList<>();

    public Photo() {
    }

    public Photo(String photoPath, long date) {
        mPhotoPath = photoPath;
        mDate = date;
    }

    public void addResult(Result result) {
        if (mResults != null) mResults.add(result);
    }

    public ArrayList<Result> getResults() {
        return mResults;
    }

    public void setResults(ArrayList<Result> mResults) {
        this.mResults = mResults;
    }

    public long getId() {
        return mId;
    }

    public void setId(long mId) {
        this.mId = mId;
    }

    public String getPhotoPath() {
        return mPhotoPath;
    }

    public void setPhotoPath(String mPhotoPath) {
        this.mPhotoPath = mPhotoPath;
    }

    public long getDate() {
        return mDate;
    }

    public void setDate(long mDate) {
        this.mDate = mDate;
    }
}
