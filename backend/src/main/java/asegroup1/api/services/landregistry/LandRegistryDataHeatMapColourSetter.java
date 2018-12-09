package asegroup1.api.services.landregistry;

import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.landregistry.LandRegistryData;

import java.util.*;

class LandRegistryDataHeatMapColourSetter {

	static List<LandRegistryData>
	setHeatMapColours(List<LandRegistryData> landRegistryDataList) throws IllegalArgumentException {
		TreeMap<Double, ArrayList<LandRegistryData>> groupedLandRegistryDataLists = groupByPricePaid(landRegistryDataList);
		List<LandRegistryData> rtnDataList = new ArrayList<>();
		Set<Map.Entry<Double, ArrayList<LandRegistryData>>> datas = groupedLandRegistryDataLists.entrySet();
		int groupedSize = datas.size();
		int groupedCount = 0;
		for (Map.Entry<Double, ArrayList<LandRegistryData>> data : datas) {
			if (data.getKey() < 0){throw new IllegalArgumentException("Values for price paid must not be negative");}
			for (LandRegistryData landRegistryData : data.getValue()) {
				double value = datas.size() <= 1 ? 0.5 : (double) groupedCount / (groupedSize - 1);
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
	static Colour getColour(double value) {
		// Make sure value is between 0 and 1
		assert (0 <= value);
		assert (1 >= value);
		return new Colour(((int) Math.round(255 * value)), (int) Math.round(255 * (1 - value)));
	}

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
