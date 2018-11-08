package asegroup1.api.models;

import java.util.Date;

public class Address {
    private String houseName;
    private String streetName;
    private String townName;
    private String postCode;

    public Address(String houseName, String streetName, String townName, String postCode) {
        this.houseName = houseName;
        this.streetName = streetName;
        this.townName = townName;
        this.postCode = postCode;
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
}
