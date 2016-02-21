package com.luismedinaweb.whatsthat.Data.model.visionapi;

import java.util.ArrayList;

/**
 * Created by lmedina on 12/10/2015.
 */
public class BoundingPoly {

    private ArrayList<Vertex> vertices = new ArrayList<>();

    public ArrayList<Vertex> getVertices() {
        return vertices;
    }

    public void setVertices(ArrayList<Vertex> vertices) {
        this.vertices = vertices;
    }
}
