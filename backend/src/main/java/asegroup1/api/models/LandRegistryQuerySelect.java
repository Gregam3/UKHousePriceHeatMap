package asegroup1.api.models;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQuerySelect {

	// list used instead of hashset, as it allows custom ordering of output request
	// data
	private ArrayList<Selectable> selectableMap;

	public LandRegistryQuerySelect(Selectable... selectables) {
		selectableMap = new ArrayList<Selectable>();
		addSelectable(selectables);
	}

	public void addSelectable(Selectable... selectables) {
		for (Selectable selectable : selectables) {
			if (!selectableMap.contains(selectable)) {
				selectableMap.add(selectable);
			}
		}
	}

	public void selectAll() {
		EnumSet.allOf(Selectable.class).forEach(v -> selectableMap.add(v));
	}

	public void removeSelectable(Selectable... selectables) {
		for (Selectable selectable : selectables) {
			selectableMap.remove(selectable);
		}
	}

	public void deselectAll() {
		selectableMap.clear();
	}

	public boolean hasSelectable(Selectable selectable) {
		return selectableMap.contains(selectable);
	}


	private String getSelectableText(Selectable selectable) {
		switch (selectable) {
			case housePriceIndex:
				return "hpi";
			case primaryAddress:
				return "paon";
			case secondaryAddress:
				return "saon";
			default:
				return selectable.name();
		}
	}

	public enum Selectable {
		propertyType, estateType, transactionDate, pricePaid, newBuild, transactionCategory, primaryAddress, secondaryAddress, street, locality, town, district, county, postcode,
		date, housePriceIndex;
	}

	public String buildQuerySelect() {
		String str = "SELECT ";
		for (Selectable selectable : selectableMap) {
			str += "?" + getSelectableText(selectable) + " ";
		}
		return str.trim();
	}

}
