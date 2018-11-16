package asegroup1.api.models.landregistry;

import java.util.ArrayList;
import java.util.EnumSet;

import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

/**
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQueryGroup {

	private ArrayList<Selectable> selectableMap;

	/**
	 * Initialise the LandRegistryQuerySelect with specified {@link Selectable
	 * selectable's}.
	 * 
	 * @param selectables to initialise the code
	 */
	public LandRegistryQueryGroup(Selectable... selectables) {
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
	public LandRegistryQueryGroup(boolean selectAll, Selectable... exceptions) {
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

	/**
	 * Get all selectables stored
	 * 
	 * @return all selectables stored
	 */
	public ArrayList<Selectable> getSelectables() {
		return selectableMap;
	}

	public String buildGroup() {
		StringBuilder groupBuilder = new StringBuilder("GROUP BY ");
		for (Selectable selectable : selectableMap) {
			groupBuilder.append("?");
			groupBuilder.append(selectable.toString());
			groupBuilder.append(" ");
		}
		return groupBuilder.toString().trim();
	}

}
