package asegroup1.api.models;

import java.util.HashSet;

/**
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQuerySelect {

	private HashSet<Selectable> selectableMap;

	public LandRegistryQuerySelect(Selectable... selectables) {
		selectableMap = new HashSet<>();
		addSelectable(selectables);
	}

	public void addSelectable(Selectable... selectables) {
		for (Selectable selectable : selectables) {
			selectableMap.add(selectable);
		}
	}

	public void removeSelectable(Selectable... selectables) {
		for (Selectable selectable : selectables) {
			selectableMap.remove(selectable);
		}
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
