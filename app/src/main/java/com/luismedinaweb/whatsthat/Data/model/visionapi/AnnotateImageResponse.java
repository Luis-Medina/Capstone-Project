package com.luismedinaweb.whatsthat.Data.model.visionapi;

import java.util.ArrayList;

/**
 * Created by lmedina on 12/10/2015.
 */
public class AnnotateImageResponse {

    private ArrayList<EntityAnnotation> labelAnnotations = new ArrayList<>();
    private Status error;

    public ArrayList<EntityAnnotation> getLabelAnnotations() {
        return labelAnnotations;
    }

    public void setLabelAnnotations(ArrayList<EntityAnnotation> labelAnnotations) {
        this.labelAnnotations = labelAnnotations;
    }

    public Status getError() {
        return error;
    }

    public void setError(Status error) {
        this.error = error;
    }
}
