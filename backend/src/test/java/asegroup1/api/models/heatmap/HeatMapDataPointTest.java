package asegroup1.api.models.heatmap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */
class HeatMapDataPointTest {
	@Test
	void testHeatMapDataPointPOJO() {
		HeatMapDataPoint heatMapDataPoint =
			new HeatMapDataPoint(0, 0, new Colour(255), 1);

		heatMapDataPoint.setLatitude(1);
		heatMapDataPoint.setLongitude(1);
		heatMapDataPoint.setColour(new Colour(55));
		heatMapDataPoint.setRadius(2);

		assertEquals(1, heatMapDataPoint.getLatitude());
		assertEquals(1, heatMapDataPoint.getLongitude());
		assertEquals(new Colour(55).getHex(),
					 heatMapDataPoint.getColour().getHex());
		assertEquals(2, heatMapDataPoint.getRadius());

		assert heatMapDataPoint.getId().length() == 36;
	}
}