package asegroup1.api.models.heatmap;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
public class HeatMapDataPoint {
    private double latitude;
    private double longitude;
    private Colour colour;

    public HeatMapDataPoint(double latitude, double longitude, Colour colour) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.colour = colour;
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

    public Colour getColour() {
        return colour;
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }
}
