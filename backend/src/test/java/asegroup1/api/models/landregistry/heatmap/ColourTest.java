package asegroup1.api.models.landregistry.heatmap;

import asegroup1.api.models.heatmap.Colour;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
class ColourTest {

    private static final Pattern COLOUR_PATTERN = Pattern.compile("#[A-Fa-f0-9]{6}");

    @Test
    void testIfGetColourAsHexGeneratesValidColour() {
        assert COLOUR_PATTERN.matcher(new Colour(255).getHex()).find();
        assert COLOUR_PATTERN.matcher(new Colour(0).getHex()).find();
    }

    @Test
    void testIfTooLargeARedValueWillCauseInvalidParameterExceptionToBeThrown() {
        assertThrows(InvalidParameterException.class, () -> new Colour(256).getHex());
        assertThrows(InvalidParameterException.class, () -> new Colour(-1).getHex());
    }

    @Test
    void testColourIsConstrainedCorrectly() {
        Colour colour = new Colour(155);

        assert colour.getBlue() == 0;
        assert colour.getGreen() == 0;
    }
}