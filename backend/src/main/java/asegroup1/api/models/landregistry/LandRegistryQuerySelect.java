package asegroup1.api.models.landregistry;

import java.util.ArrayList;
import java.util.EnumSet;

/**
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQuerySelect {

	private ArrayList<Selectable> selectableMap;

	/**
	 * Initialise the LandRegistryQuerySelect with specified {@link Selectable
	 * selectable's}.
	 * 
	 * @param selectables to initialise the code
	 */
	public LandRegistryQuerySelect(Selectable... selectables) {
		selectableMap = new ArrayList<Selectable>();
		select(selectables);
	}

	/**
	 * Initialise the LandRegistryQuerySelect to contain all {@link Selectable
	 * selectable's}, except the specified exceptions.
	 * 
	 * @param selectAll  If true the query will be initialised to contain all
	 *                   values, if false the query will be initialised to be empty
	 * @param exceptions Any exceptions to the specified rule.
	 */
	public LandRegistryQuerySelect(boolean selectAll, Selectable... exceptions) {
		selectableMap = new ArrayList<Selectable>();
		if (selectAll) {
			selectAll();
			deselect(exceptions);
		} else {
			select(exceptions);
		}
	}

	/**
	 * Select the specified {@link Selectable selectable's}.
	 * 
	 * @param selectables to select
	 */
	public void select(Selectable... selectables) {
		for (Selectable selectable : selectables) {
			if (!selectableMap.contains(selectable)) {
				selectableMap.add(selectable);
			}
		}
	}

	/**
	 * Select all {@link Selectable selectable's}.
	 */
	public void selectAll() {
		EnumSet.allOf(Selectable.class).forEach(v -> selectableMap.add(v));
	}

	/**
	 * Deselect the specified {@link Selectable selectable's}.
	 * 
	 * @param selectables to deselect
	 */
	public void deselect(Selectable... selectables) {
		for (Selectable selectable : selectables) {
			selectableMap.remove(selectable);
		}
	}

	/**
	 * Deselect all {@link Selectable selectable's}.
	 */
	public void deselectAll() {
		selectableMap.clear();
	}

	/**
	 * Check if the specified {@link Selectable} is selected.
	 * 
	 * @param selectable to check
	 * @return true, if the specified selectable is selected
	 */
	public boolean hasSelectable(Selectable selectable) {
		return selectableMap.contains(selectable);
	}

	/**
	 * Toggle the specified {@link Selectable}. If the specified value is selected,
	 * it will be deselected. Otherwise it will be selected.
	 * 
	 * @param selectable to toggle
	 */
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

	/**
	 * Builds and returns the SELECT section of the query.
	 * 
	 * @return the SELECT section of the query
	 */
	public String buildQuerySelect() {
		StringBuilder selectStringBuilder = new StringBuilder("SELECT ");
		for (Selectable selectable : selectableMap) {
			selectStringBuilder.append("?" + selectable.toString() + " ");
		}
		return selectStringBuilder.toString().trim();
	}

	/**
	 * Builds and returns the SELECT section of the query. This should be used in
	 * conjunction with {@link LandRegistryQueryConstraint#buildUniqueGrouping()}.
	 * The following {@link Selectable selectable's} will be auto selected paon,
	 * saon, street, postcode, transactionDate.
	 * 
	 * @return the SELECT section of the query
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
