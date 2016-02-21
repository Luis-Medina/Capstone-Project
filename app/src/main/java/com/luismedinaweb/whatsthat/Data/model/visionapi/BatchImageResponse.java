package com.luismedinaweb.whatsthat.Data.model.visionapi;

import java.util.ArrayList;

/**
 * Created by lmedina on 12/10/2015.
 */
public class BatchImageResponse {

    private ArrayList<AnnotateImageResponse> responses = new ArrayList<>();

    public ArrayList<AnnotateImageResponse> getResponses() {
        return responses;
    }

    public void setResponses(ArrayList<AnnotateImageResponse> responses) {
        this.responses = responses;
    }
}
