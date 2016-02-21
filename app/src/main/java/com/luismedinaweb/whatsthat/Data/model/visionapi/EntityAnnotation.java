package com.luismedinaweb.whatsthat.Data.model.visionapi;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by lmedina on 12/10/2015.
 */
public class EntityAnnotation implements Serializable {

    private static final long serialVersionUID = 1L;
    private String mid;
    private String locale;
    private String description;
    private float score;
    private float confidence;
    private float topicality;
    private BoundingPoly boundingPoly;
    private ArrayList<LocationInfo> locations = new ArrayList<>();

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getMid() {
        return mid;
    }

    public void setMid(String mid) {
        this.mid = mid;
    }

    public String getLocale() {
        return locale;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(float confidence) {
        this.confidence = confidence;
    }

    public double getTopicality() {
        return topicality;
    }

    public void setTopicality(float topicality) {
        this.topicality = topicality;
    }

    public BoundingPoly getBoundingPoly() {
        return boundingPoly;
    }

    public void setBoundingPoly(BoundingPoly boundingPoly) {
        this.boundingPoly = boundingPoly;
    }

    public ArrayList<LocationInfo> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LocationInfo> locations) {
        this.locations = locations;
    }
}
