package asegroup1.api.models.heatmap;

import java.security.InvalidParameterException;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
public class Colour {
    private int red;

    public Colour(int red) {
        this.red = red;
    }

    public int getGreen() {
        return 0;
    }

    public int getRed() {
        return red;
    }

    public void setRed(int green) {
        this.red = green;
    }

    public int getBlue() {
        return 0;
    }


    @Override
    public String toString() {
        return getColourAsHex();
    }

    public String getColourAsHex() {
        return "#" + toHexColourString(red) +  toHexColourString(getGreen()) + toHexColourString(getBlue());
    }

    private String toHexColourString(int value) {
        if (value > 255 || value < 0) {
            throw new InvalidParameterException("Colour value must be between 0-255");
        }

        String hexValue = Integer.toHexString(value);

        return (hexValue.length() < 2) ? "0" + hexValue : hexValue;
    }
}
