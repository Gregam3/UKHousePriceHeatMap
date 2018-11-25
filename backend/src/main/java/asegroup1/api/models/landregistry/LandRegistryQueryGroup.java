package asegroup1.api.models.landregistry;

import java.util.ArrayList;

import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

/**
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQueryGroup {

	private ArrayList<String> selectableMap;

	/**
	 * Initialise the LandRegistryQuerySelect with specified {@link Selectable
	 * selectable's}.
	 * 
	 * @param selectables to initialise the code
	 */
	public LandRegistryQueryGroup(String... selectables) {
		selectableMap = new ArrayList<String>();
		select(selectables);
	}

	/**
	 * Select the specified {@link Selectable selectable's}.
	 * 
	 * @param selectables to select
	 */
	public void select(String... selectables) {
		for (String selectable : selectables) {
			if (!selectableMap.contains(selectable)) {
				selectableMap.add(selectable);
			}
		}
	}

	/**
	 * Deselect the specified {@link Selectable selectable's}.
	 * 
	 * @param selectables to deselect
	 */
	public void deselect(String... selectables) {
		for (String selectable : selectables) {
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
	public boolean hasSelectable(String selectable) {
		return selectableMap.contains(selectable);
	}

	/**
	 * Toggle the specified {@link Selectable}. If the specified value is selected,
	 * it will be deselected. Otherwise it will be selected.
	 * 
	 * @param selectable to toggle
	 */
	public void toggleSelectable(String selectable) {
		if (hasSelectable(selectable)) {
			deselect(selectable);
		} else {
			select(selectable);
		}
	}

	/**
	 * Get all selectables stored
	 * 
	 * @return all selectables stored
	 */
	public ArrayList<String> getSelectables() {
		return selectableMap;
	}

	public String buildGroup() {
		StringBuilder groupBuilder = new StringBuilder("GROUP BY ");
		for (String selectable : selectableMap) {
			groupBuilder.append("?");
			groupBuilder.append(selectable);
			groupBuilder.append(" ");
		}
		return groupBuilder.toString().trim();
	}

}
