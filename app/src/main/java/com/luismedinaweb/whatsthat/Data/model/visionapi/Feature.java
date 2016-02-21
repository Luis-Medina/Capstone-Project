package com.luismedinaweb.whatsthat.Data.model.visionapi;

/**
 * Created by lmedina on 12/10/2015.
 */
public class Feature {

    private int maxResults;
    private Type type;

    public int getMaxResults() {
        return maxResults;
    }

    public void setMaxResults(int maxResults) {
        this.maxResults = maxResults;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public enum Type {
        TYPE_UNSPECIFIED,
        FACE_DETECTION,
        LANDMARK_DETECTION,
        LOGO_DETECTION,
        LABEL_DETECTION,
        TEXT_DETECTION,
        SAFE_SEARCH_DETECTION;
    }

}
