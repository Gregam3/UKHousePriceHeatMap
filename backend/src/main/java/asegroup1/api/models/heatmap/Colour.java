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

    public Colour(int colourValue) {
        int val1 = colourValue * 3;

        if (val1 > 400) setRed(colourValue);
        else if (val1 > 200 && val1 < 400) {
            setRed((int) (colourValue * 1.9));
            setGreen((int) (colourValue * 1.9));
        } else if (val1 < 200) setGreen((int) (colourValue * 3.85));
    }

    public void setGreen(int green) {
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

    public void setRed(int green) {
        this.red = green;
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
        return "rgba(" + getRed() + "," + getGreen() + "," + getBlue() + ",1)";
    }

    public String getHex() {
        return "#" + toHexColourString(getRed()) + toHexColourString(getGreen()) + toHexColourString(getBlue());
    }

    private String toHexColourString(int value) {
        if (value > 255 || value < 0) {
            throw new InvalidParameterException("Colour value must be between 0-255");
        }

        String hexValue = Integer.toHexString(value);

        return (hexValue.length() < 2) ? "0" + hexValue : hexValue;
    }
}
