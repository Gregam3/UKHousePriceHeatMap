package asegroup1.api.models.landregistry;

import java.util.LinkedHashMap;

import asegroup1.api.models.landregistry.LandRegistryQuery.Aggrigation;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

public class LandRegistryQuerySelect {

	private LinkedHashMap<String, SelectObj> selectValues;

	public LandRegistryQuerySelect(Selectable... selectables) {
		selectValues = new LinkedHashMap<String, SelectObj>();
		for (Selectable selectable : selectables) {
			addSelectValue(selectable, Aggrigation.SAMPLE);
		}
	}

	public void addSelectValue(String referenceName, Aggrigation aggrigation, String aggrigationResult) {
		selectValues.put(referenceName, new SelectObj(referenceName, aggrigationResult, aggrigation));
	}

	public void addSelectValue(String referenceName) {
		addSelectValue(referenceName, Aggrigation.NONE, null);
	}

	public void addSelectValue(Selectable reference, Aggrigation aggrigation) {
		addSelectValue(reference.toString(), aggrigation, capitalise(reference.toString()));
	}

	private String capitalise(String str) {
		return str.substring(0, 1).toUpperCase() + str.substring(1);
	}

	public boolean hasValue(String reference) {
		return selectValues.containsKey(reference);
	}

	public boolean removeValue(String reference) {
		if (hasValue(reference)) {
			selectValues.remove(reference);
			return true;
		} else {
			return false;
		}
	}

	private SelectObj getSelectObj(String referenceName) {
		return selectValues.get(referenceName);
	}

	public String[] getSelectValues(String reference) {
		SelectObj obj = getSelectObj(reference);
		return obj == null ? null : new String[] { obj.referenceName, obj.aggrigation.toString(), obj.aggrigationName };
	}

	public String getSelectValueAggrigationName(String reference) {
		SelectObj obj = getSelectObj(reference);
		return obj == null ? null : getSelectValueAggrigationName(obj);
	}

	public Aggrigation getSelectValueAggrigation(String reference) {
		SelectObj obj = getSelectObj(reference);
		return obj == null ? null : getSelectValueAggrigation(obj);
	}

	public static String getSelectValueAggrigationName(SelectObj reference) {
		return reference.aggrigationName;
	}

	public static String getSelectValueReferencenName(SelectObj reference) {
		return reference.referenceName;
	}

	public static Aggrigation getSelectValueAggrigation(SelectObj reference) {
		return reference.aggrigation;
	}

	public LinkedHashMap<String, SelectObj> getSelectValues() {
		return selectValues;
	}

	/**
	 * Builds and returns the SELECT section of the query.
	 * 
	 * @return the SELECT section of the query
	 */
	public String buildQuerySelect(boolean ignoreAggrigation) {
		StringBuilder selectStringBuilder = new StringBuilder("SELECT ");
		for (SelectObj selectable : selectValues.values()) {
			selectStringBuilder.append(selectable.toString(ignoreAggrigation) + " ");
		}
		return selectStringBuilder.toString().trim();
	}



	private class SelectObj {
		String referenceName;
		String aggrigationName;
		Aggrigation aggrigation;


		public SelectObj(String referenceName, String aggrigationName, Aggrigation aggrigation) {
			this.referenceName = referenceName;
			this.aggrigationName = aggrigationName;
			this.aggrigation = aggrigation;
		}

		public String toString(boolean ignoreAggrigation) {
			String structure = null;
			if (ignoreAggrigation) {
				structure = "?%s";
			} else {

				switch (aggrigation) {
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
			return String.format(structure, referenceName, aggrigationName);

		}

		@Override
		public String toString() {
			return toString(false);
		}
	}
}
