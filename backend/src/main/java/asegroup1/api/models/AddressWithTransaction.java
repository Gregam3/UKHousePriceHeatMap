package asegroup1.api.models;

import java.util.Date;

public class AddressWithTransaction extends Address {

    private long price;
    private Date timestamp;

    public AddressWithTransaction(String houseName, String streetName, String townName, String postCode, Date timestamp, long price) {
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
