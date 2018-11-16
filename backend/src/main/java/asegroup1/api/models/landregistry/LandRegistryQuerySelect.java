package asegroup1.api.models.landregistry;

import java.util.LinkedHashMap;

import asegroup1.api.models.landregistry.LandRegistryQuery.Aggrigation;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

public class LandRegistryQuerySelect {

	private LinkedHashMap<Selectable, Aggrigation> selectValues;


	public LandRegistryQuerySelect() {
		selectValues = new LinkedHashMap<>();
	}

	public LandRegistryQuerySelect(Selectable... selectables) {
		selectValues = new LinkedHashMap<>();
		for (Selectable selectable : selectables) {
			setSelectValue(selectable, Aggrigation.SAMPLE);
		}
	}


	public void setSelectValue(Selectable selectable, Aggrigation aggrigation) {
		selectValues.put(selectable, aggrigation);
	}

	public boolean hasValue(Selectable selectable) {
		return selectValues.containsKey(selectable);
	}

	public boolean removeValue(Selectable selectable) {
		if (hasValue(selectable)) {
			selectValues.remove(selectable);
			return true;
		} else {
			return false;
		}
	}

	public Aggrigation getSelectValues(Selectable selectable) {
		return selectValues.get(selectable);
	}


	/**
	 * Builds and returns the SELECT section of the query.
	 * 
	 * @return the SELECT section of the query
	 */
	public String buildQuerySelect(boolean ignoreAggrigation) {
		StringBuilder selectStringBuilder = new StringBuilder("SELECT ");
		for (Selectable selectable : selectValues.keySet()) {
			selectStringBuilder.append(getSelectValueString(selectable, ignoreAggrigation) + " ");
		}
		return selectStringBuilder.toString().trim();
	}

	private String getSelectValueString(Selectable selectable, boolean ignoreAggrigation) {
		if(!hasValue(selectable)) {
			return "";
		}
		String structure = null;
		if(ignoreAggrigation) {
			return structure = "?%s";
		}else {

			switch (selectValues.get(selectable)) {
				case AVG:
					structure = "(AVG(?%s) AS ?%s)";
					break;
				case COUNT:
					structure = "(COUNT(?%s) AS ?%s)";
					break;
				case MAX:
					structure = "(MAX(?%s) AS ?%s)";
					break;
				case MIN:
					structure = "(MIN(?%s) AS ?%s)";
					break;
				case NONE:
					structure = "?%s";
					break;
				case SAMPLE:
					structure = "(SAMPLE(?%s) AS ?%s)";
					break;
				case SUM:
					structure = "(SUM(?%s) AS ?%s)";
					break;
				default:
					throw new IllegalArgumentException("Unexpected enum");
			}
		}
		String selectStr = selectable.toString();
		return String.format(structure, selectStr, selectStr.substring(0, 1).toUpperCase() + selectStr.substring(1));
		
	}

}
