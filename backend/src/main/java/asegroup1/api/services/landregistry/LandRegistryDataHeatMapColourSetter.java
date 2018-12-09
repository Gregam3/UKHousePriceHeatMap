package asegroup1.api.services.landregistry;

import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.landregistry.LandRegistryData;

import java.awt.*;
import java.util.*;
import java.util.List;

class LandRegistryDataHeatMapColourSetter {

	static List<LandRegistryData>
	setHeatMapColours(List<LandRegistryData> landRegistryDataList) throws IllegalArgumentException {

		TreeMap<Double, ArrayList<LandRegistryData>> groupedLandRegistryDataLists = groupByPricePaid(landRegistryDataList);
		ArrayList<LandRegistryData> rtnDataList = new ArrayList<>();
		Set<Map.Entry<Double, ArrayList<LandRegistryData>>> dataList = groupedLandRegistryDataLists.entrySet();

		int groupedSize = dataList.size();
		int groupedCount = 0;

		for (Map.Entry<Double, ArrayList<LandRegistryData>> data : dataList) {
			if (data.getKey() < 0) {throw new IllegalArgumentException("Values for price paid must not be negative");}
			for (LandRegistryData landRegistryData : data.getValue()) {
				double value = dataList.size() <= 1 ? 0.5 : (double) groupedCount / (groupedSize - 1);
				landRegistryData.setColour(getColour(value));
				rtnDataList.add(landRegistryData);
			}
			groupedCount++;
		}

		return rtnDataList;
	}

	/**
	 * Converts the 0 to 1 value to a colour between green and red
	 *
	 * @param value percentage value as a decimal between 0 and 1(inclusive)
	 * @return a Color object between green and red
	 */
	private static Colour getColour(double value) {
		// Make sure value is between 0 and 1
		assert (0 <= value);
		assert (1 >= value);
		Color colour = Color.getHSBColor((float) ((1-value)*0.4), 1, (float) 0.5); // These are percentage values represented as decimals
		return new Colour(colour.getRed(), colour.getGreen(), colour.getBlue());
	}

	/**
	 * Takes a list of LandRegistryData and groups them by price paid so that items with the same value are given the same colour
	 *
	 * @param landRegistryDataList List of land registry data to be grouped
	 * @return A TreeMap(Which is inherently sorted) of lists of land registry data where the key is the price paid
	 */
	private static TreeMap<Double, ArrayList<LandRegistryData>> groupByPricePaid(List<LandRegistryData> landRegistryDataList) {
		TreeMap<Double, ArrayList<LandRegistryData>> groupedLandRegistryDataList = new TreeMap<>();
		for (LandRegistryData data : landRegistryDataList) {
			if (!groupedLandRegistryDataList.containsKey(data.getPricePaid())) {
				groupedLandRegistryDataList.put(data.getPricePaid(), new ArrayList<>(Collections.singletonList(data)));
			} else {
				groupedLandRegistryDataList.get(data.getPricePaid()).add(data);
			}
		}

		return groupedLandRegistryDataList;
	}
}
