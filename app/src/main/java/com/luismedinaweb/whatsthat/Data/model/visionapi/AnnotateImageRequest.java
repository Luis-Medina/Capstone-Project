package com.luismedinaweb.whatsthat.Data.model.visionapi;

import java.util.ArrayList;

/**
 * Created by lmedina on 12/10/2015.
 */
public class AnnotateImageRequest {

    private Image image;
    private ArrayList<Feature> features;

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public ArrayList<Feature> getFeature() {
        return features;
    }

    public void setFeature(ArrayList<Feature> features) {
        this.features = features;
    }
}
