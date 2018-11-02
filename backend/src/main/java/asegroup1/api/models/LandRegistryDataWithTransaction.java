package asegroup1.api.models;

import java.util.Date;

public class LandRegistryDataWithTransaction extends LandRegistryData {

    private long price;
    private Date timestamp;

    public LandRegistryDataWithTransaction(String houseName, String streetName, String townName, String postCode, Date timestamp, long price) {
        super(houseName, streetName, townName, postCode);
        this.price = price;
        this.timestamp = timestamp;
    }

    public long getPrice() {
        return price;
    }

    public void setPrice(long price) {
        this.price = price;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
