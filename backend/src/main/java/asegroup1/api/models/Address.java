package asegroup1.api.models;

public class Address {
    private String houseName;
    private String streetName;
    private String townName;
    private String postCode;
    private Double latitude;
    private Double longitude;

    public Address(String houseName, String streetName, String townName, String postCode) {
        this.houseName = houseName;
        this.streetName = streetName;
        this.townName = townName;
        this.postCode = postCode;
    }

    public Address(String houseName, String streetName, String townName, String postCode, Double latitude, Double longitude) {
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

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
