package asegroup1.api.models;

import java.util.Date;

public class HouseTransactionData {
    private String paon;
    private String street;
    private String town;
    private String county;
    private String postcode;
    private long amount;
    private Date date;

    public HouseTransactionData(String paon, String street, String town, String county, String postcode, long amount, Date date) {
        this.paon = paon;
        this.street = street;
        this.town = town;
        this.county = county;
        this.postcode = postcode;
        this.amount = amount;
        this.date = date;
    }

    public String getPaon() {
        return paon;
    }

    public void setPaon(String paon) {
        this.paon = paon;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public String getTown() {
        return town;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getCounty() {
        return county;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
    }

    public long getAmount() {
        return amount;
    }

    public void setAmount(long amount) {
        this.amount = amount;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
