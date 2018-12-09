package asegroup1.api.models.heatmap;

import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
class ColourTest {

	private final Random random = new Random(8312595207343625996L);

	private static final Pattern COLOUR_PATTERN =
		Pattern.compile("#[A-Fa-f0-9]{6}");

	@Test
	void testIfGetColourAsHexGeneratesValidColour() {
		assert COLOUR_PATTERN.matcher(new Colour(255).getHex()).find();
		assert COLOUR_PATTERN.matcher(new Colour(0).getHex()).find();
	}

	@Test
	void testIfTooLargeARedValueWillCauseInvalidParameterExceptionToBeThrown() {
		assertThrows(InvalidParameterException.class,
					 () -> new Colour(256).getHex());
		assertThrows(InvalidParameterException.class,
					 () -> new Colour(-1).getHex());
	}

	@Test
	void testBlueValueIsConstrainedCorrectly() {
		List<Colour> colours = new ArrayList<>();

		for (int i = 0; i < 10; i++) {
			colours.add(new Colour(random.nextInt(255) + 1));
		}

		assert colours.stream()
			.filter(c -> c.getBlue() > 0)
			.collect(Collectors.toList())
			.isEmpty();
	}
}