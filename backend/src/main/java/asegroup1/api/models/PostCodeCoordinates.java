package asegroup1.api.models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 * 
 * @author Rikkey Paal
 */
@Entity
@Table(name = "postcodelatlng")
public class PostCodeCoordinates {
    private String postcode;
    private double latitude;
    private double longitude;
	private Long averageprice;

    public PostCodeCoordinates() {
    }

    @Id
    public String getPostcode() {
        return postcode;
    }

    public void setPostcode(String postcode) {
        this.postcode = postcode;
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

	public Long getAverageprice() {
		return averageprice;
	}

	public void setAverageprice(Long averageprice) {
		this.averageprice = averageprice;
	}
}
