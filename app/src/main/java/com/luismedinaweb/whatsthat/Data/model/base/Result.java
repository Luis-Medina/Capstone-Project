package com.luismedinaweb.whatsthat.Data.model.base;

import java.io.Serializable;

/**
 * Created by lmedina on 2/16/2016.
 */
public class Result implements Serializable {

    private static final long serialVersionUID = 1L;
    private long mPhotoId = 0;
    private String mLabel = "";
    private float mScore = 0;

    public Result() {
    }

    public Result(String label, float score) {
        mLabel = label;
        mScore = score;
    }

    public long getPhotoId() {
        return mPhotoId;
    }

    public void setPhotoId(long mPhotoId) {
        this.mPhotoId = mPhotoId;
    }

    public String getLabel() {
        return mLabel;
    }

    public void setLabel(String mLabel) {
        this.mLabel = mLabel;
    }

    public float getScore() {
        return mScore;
    }

    public void setScore(float mScore) {
        this.mScore = mScore;
    }
}
