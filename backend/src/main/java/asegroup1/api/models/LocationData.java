package asegroup1.api.models;


import java.io.Serializable;
import java.sql.Timestamp;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "location_data")
public class LocationData implements Serializable {

	@Id
	@Column(name = "USER_ID")
	private String userId;
	@Id
	@Column(name = "TIMELOG")
	private Timestamp timelog;
	@Column(name = "LONGITUDE")
	private float longitude;
	@Column(name = "LATITUDE")
	private float latitude;
	@Column(name = "ALTITUDE")
	private float altitude;
	@Column(name = "DELIVERED")
	private boolean delivered;

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public Timestamp getTimelog() {
		return timelog;
	}

	public void setTimelog(Timestamp timelog) {
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

	public boolean getDelivered() {
		return delivered;
	}

	public void setDelivered(boolean delivered) {
		this.delivered = delivered;
	}


}
