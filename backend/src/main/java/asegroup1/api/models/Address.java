package asegroup1.api.models;

import java.util.Date;

public class Address {
    private String houseName;
    private String streetName;
    private String townName;
    private String postCode;
    private double latitude;
    private double longitude;

    public Address(String houseName, String streetName, String townName, String postCode) {
        this.houseName = houseName;
        this.streetName = streetName;
        this.townName = townName;
        this.postCode = postCode;
        //-1 Represents not fetched, not could not retrieve
        this.latitude = -1;
        this.longitude = -1;
    }

    public Address(String houseName, String streetName, String townName, String postCode, double latitude, double longitude) {
        this.houseName = houseName;
        this.streetName = streetName;
        this.townName = townName;
        this.postCode = postCode;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getHouseName() {
        return houseName;
    }

    public void setHouseName(String houseName) {
        this.houseName = houseName;
    }

    public String getStreetName() {
        return streetName;
    }

    public void setStreetName(String streetName) {
        this.streetName = streetName;
    }

    public String getTownName() {
        return townName;
    }

    public void setTownName(String townName) {
        this.townName = townName;
    }

    public String getPostCode() {
        return postCode;
    }

    public void setPostCode(String postCode) {
        this.postCode = postCode;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
