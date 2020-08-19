package com.example.nicholas.myapplication;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by h on 31/10/15.
 */
public class Shelter implements Comparable {
    private String name;
    private LatLng latLng;
    private Double dist;

    public Shelter(String name, double longditude, double latitude, double distance){
        this.name = name;
        latLng = new LatLng(longditude, latitude);
        dist = distance;
    }

    public String getName(){
        return name;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public Double getDistance() {
        return dist;
    }

    public boolean equals(Shelter obj) {
        if (this.dist == dist) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int compareTo(Object shel) {
        Double d = new Double(this.dist - ((Shelter) shel).dist);
        return d.intValue();
    }

}
