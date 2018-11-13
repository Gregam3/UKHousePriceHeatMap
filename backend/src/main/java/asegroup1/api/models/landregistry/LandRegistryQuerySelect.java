package asegroup1.api.models.landregistry;

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
		select(selectables);
	}

	public LandRegistryQuerySelect(boolean selectAll, Selectable... exceptions) {
		selectableMap = new ArrayList<Selectable>();
		if (selectAll) {
			selectAll();
			deselect(exceptions);
		} else {
			select(exceptions);
		}
	}

	public void select(Selectable... selectables) {
		for (Selectable selectable : selectables) {
			if (!selectableMap.contains(selectable)) {
				selectableMap.add(selectable);
			}
		}
	}

	public void selectAll() {
		EnumSet.allOf(Selectable.class).forEach(v -> selectableMap.add(v));
	}

	public void deselect(Selectable... selectables) {
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

	public void toggleSelectable(Selectable selectable) {
		if (hasSelectable(selectable)) {
			deselect(selectable);
		} else {
			select(selectable);
		}
	}


	public enum Selectable {
		propertyType, estateType, transactionDate, pricePaid, newBuild, transactionCategory, paon, saon, street, locality, town, district, county, postcode;
	}


	public String buildQuerySelect() {
		StringBuilder selectStringBuilder = new StringBuilder("SELECT ");
		for (Selectable selectable : selectableMap) {
			selectStringBuilder.append("?" + selectable.toString() + " ");
		}
		return selectStringBuilder.toString().trim();
	}

	/**
	 * The following selectables will be auto selected paon, saon, street, postcode,
	 * transactionDate
	 * 
	 * @param getUnique
	 * @return
	 */
	public String buildQuerySelectUnique() {

		LandRegistryQuerySelect tmp = new LandRegistryQuerySelect(selectableMap.toArray(new Selectable[selectableMap.size()]));
		tmp.deselect(Selectable.paon, Selectable.saon, Selectable.street, Selectable.postcode, Selectable.transactionDate);

		StringBuilder selectStringBuilder = new StringBuilder("SELECT ?paon ?saon ?street ?postcode (max(?transactionDate) AS ?TransactionDate) ");
		for (Selectable selectable : tmp.selectableMap) {
			String selectStr = selectable.toString();
			selectStringBuilder.append("(SAMPLE(?" + selectStr + ") AS ?" + selectStr.substring(0, 1).toUpperCase() + selectStr.substring(1) + ") ");
		}
		return selectStringBuilder.toString().trim();
	}

}
