package asegroup1.api.services.landregistry;

import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.landregistry.LandRegistryData;

import java.util.List;

public class LandRegistryDataHeatMapColourSetter {

	List<LandRegistryData> SetHeatMapColours(List<LandRegistryData> landRegistryData){
		landRegistryData.sort((o1, o2) -> Double.compare(o2.getPricePaid(), o1.getPricePaid()));
		for (int i = 0; i < landRegistryData.size(); i++){
			LandRegistryData data = landRegistryData.get(i);
			data.setColour(GetColour(i/(landRegistryData.size()-1)));
			landRegistryData.set(i, data);
		}
		return landRegistryData;


	}

	/**
	 * Converts the 0 to 1 value to a colour between green and red
	 * @param value percentage value as a decimal between 0 and 1(inclusive)
	 * @return a Color object between green and red
	 */
	private Colour GetColour(double value){
		// Make sure value is between 0 and 1
		assert(0 <= value);
		assert(1 >= value);
		return new Colour(((int) Math.round(255 * value)), (int) Math.round(255*(1-value)));
	}

}
