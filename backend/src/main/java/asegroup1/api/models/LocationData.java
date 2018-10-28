package asegroup1.api.models;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "location_data")
public class UserData {

    @Id
    @Column(name="USER_ID")
    private String userId;
    @Id
    @Column(name="TIMELOG")
    private DateTime timelog;
    @Column(name="LONGITUDE")
    private float longitude;
    @Column(name="LATITUDE")
    private float latitude;
    @Column(name="ALTITUDE")
    private float altitude;
    @Column(name="DELIVERED")
    private float delivered;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public DateTime getTimelog() {
        return timelog;
    }

    public void setTimelog(DateTime timelog) {
        this.timelog = timelog;
    }

    public float getLongitude() {
        return longitude;
    }

    public void setLongitude(float longitude) {
        this.longitude = longitude;
    }

    public float getLatitude() {
        return latitude;
    }

    public void setLatitude(float latitude) {
        this.latitude = latitude;
    }

    public float getAltitude() {
        return altitude;
    }

    public void setAltitude(float altitude) {
        this.altitude = altitude;
    }

    public float getDelivered() {
        return delivered;
    }

    public void setDelivered(float delivered) {
        this.delivered = delivered;
    }


}
