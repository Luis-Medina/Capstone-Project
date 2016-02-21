package com.luismedinaweb.whatsthat.Data.model.visionapi;

import java.util.ArrayList;

/**
 * Created by lmedina on 12/10/2015.
 */
public class BatchImageRequest {

    private ArrayList<AnnotateImageRequest> requests = new ArrayList<>();

    public ArrayList<AnnotateImageRequest> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<AnnotateImageRequest> requests) {
        this.requests = requests;
    }
}
