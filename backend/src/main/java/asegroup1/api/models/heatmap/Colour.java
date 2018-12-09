package asegroup1.api.models.heatmap;

import java.security.InvalidParameterException;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
public class Colour {
    private int red;
    private int green;

	private final static double RED_SCALING = 2.98;

	private final static double SHADES_OF_COLOURS = 3.0;

	private static final int MAX_COLOUR_VAL = 255;
	private static final double YELLOW_BRIGHTENING_COEFFICIENT =
		MAX_COLOUR_VAL / ((MAX_COLOUR_VAL * RED_SCALING) / SHADES_OF_COLOURS);
	private static final double GREEN_BRIGHTENING_COEFFICIENT =
		MAX_COLOUR_VAL / ((MAX_COLOUR_VAL) / SHADES_OF_COLOURS);

	public Colour(int colourValue) throws InvalidParameterException {
		int colourGenValue = (int)(colourValue * SHADES_OF_COLOURS);

		// Tweak to change how much read appears
		double redScaling = 2.98;

		if (colourGenValue > MAX_COLOUR_VAL * redScaling) {
			setRed(colourValue);
		} else if (colourGenValue > MAX_COLOUR_VAL &&
				   colourGenValue < MAX_COLOUR_VAL * redScaling) {
			setRed((int)(colourValue * YELLOW_BRIGHTENING_COEFFICIENT));
			setGreen((int)(colourValue * YELLOW_BRIGHTENING_COEFFICIENT));
		} else {
			setGreen((int)(colourValue * GREEN_BRIGHTENING_COEFFICIENT));
		}
	}

    public void setGreen(int green) {
		if (isColourValueValid(green))
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

	public void setRed(int red) {
		if (isColourValueValid(red))
			this.red = red;
	}

	private boolean isColourValueValid(int value) {
		if (value > 255 || value < 0) {
			throw new InvalidParameterException(
				"Colour value must be between 0-255");
		}

		return true;
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
		return "rgba(" + getRed() + "," + getGreen() + "," + getBlue() +
			",0.75)";
	}

    public String getHex() {
        return "#" + toHexColourString(getRed()) + toHexColourString(getGreen()) + toHexColourString(getBlue());
    }

    private String toHexColourString(int value) {
        String hexValue = Integer.toHexString(value);

        return (hexValue.length() < 2) ? "0" + hexValue : hexValue;
    }
}
