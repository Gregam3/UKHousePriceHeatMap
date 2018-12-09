package asegroup1.api.models.heatmap;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.security.InvalidParameterException;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
public class Colour {
    private int red;
    private int green;

    public Colour(int colourValue) throws InvalidParameterException{
        int val1 = colourValue * 3;

        if (val1 > 400) setRed(colourValue);
        else if (val1 > 200 && val1 < 400) {
            setRed((int) (colourValue * 1.9));
            setGreen((int) (colourValue * 1.9));
        } else if (val1 < 200) setGreen((int) (colourValue * 3.85));
    }

    /**
     * Sets up the colours if the red and green values are already set
     * @param red the integer value for red between 0 and 255
     * @param green the integer value for green between 0 and 255
     * @throws InvalidParameterException if the values are between 0 and 255 this exception is thrown
     */
    public Colour(int red, int green) throws InvalidParameterException{
        setRed(red);
        setGreen(green);
    }

    public void setGreen(int green) throws InvalidParameterException{
        if (green > 255 || green < 0) {
            throw new InvalidParameterException("Colour value must be between 0-255");
        }
        this.green = green;
    }

    @JsonIgnore
    public int getGreen() {
        return green;
    }

    @JsonIgnore
    public int getRed() {
        return red;
    }

    public void setRed(int red) throws InvalidParameterException{
        if (red > 255 || red < 0) {
            throw new InvalidParameterException("Colour value must be between 0-255");
        }
        this.red = red;
    }

    @JsonIgnore
    public int getBlue() {
        return 0;
    }


    @Override
    public String toString() {
        return getHex();
    }

    public String getRGBA() {
		return "rgba(" + getRed() + "," + getGreen() + "," + getBlue() + ",0.75)";
    }

    public String getHex() {
        return "#" + toHexColourString(getRed()) + toHexColourString(getGreen()) + toHexColourString(getBlue());
    }

    private String toHexColourString(int value) throws InvalidParameterException{
        if (value > 255 || value < 0) {
            throw new InvalidParameterException("Colour value must be between 0-255");
        }

        String hexValue = Integer.toHexString(value);

        return (hexValue.length() < 2) ? "0" + hexValue : hexValue;
    }
}
