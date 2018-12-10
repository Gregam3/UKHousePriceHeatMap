package asegroup1.api.models.landregistry;

import java.util.LinkedHashMap;

import asegroup1.api.models.landregistry.LandRegistryQuery.Aggregation;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

public class LandRegistryQuerySelect {

	private LinkedHashMap<String, SelectObj> selectValues;

	public LandRegistryQuerySelect(Selectable... selectables) {
		selectValues = new LinkedHashMap<String, SelectObj>();
		for (Selectable selectable : selectables) {
			addSelectValue(selectable, Aggregation.SAMPLE);
		}
	}

	public void addSelectValue(String referenceName, Aggregation aggregation, String aggregationResult) {
		selectValues.put(referenceName, new SelectObj(referenceName, aggregationResult, aggregation));
	}

	public void addSelectValue(String referenceName) {
		addSelectValue(referenceName, Aggregation.NONE, null);
	}

	public void addSelectValue(Selectable reference, Aggregation aggregation) {
		addSelectValue(reference.toString(), aggregation, capitalise(reference.toString()));
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

	SelectObj getSelectObj(String referenceName) {
		return selectValues.get(referenceName);
	}

	public String[] getSelectValues(String reference) {
		SelectObj obj = getSelectObj(reference);
		return obj == null ? null : new String[] { obj.referenceName, obj.aggregation.toString(), obj.aggregationName };
	}

	public String getSelectValueAggregationName(String reference) {
		SelectObj obj = getSelectObj(reference);
		return obj == null ? null : getSelectValueAggregationName(obj);
	}

	public Aggregation getSelectValueAggregation(String reference) {
		SelectObj obj = getSelectObj(reference);
		return obj == null ? null : getSelectValueAggregation(obj);
	}

	public static String getSelectValueAggregationName(SelectObj reference) {
		return reference.aggregationName;
	}

	public static String getSelectValueReferencenName(SelectObj reference) {
		return reference.referenceName;
	}

	public static Aggregation getSelectValueAggregation(SelectObj reference) {
		return reference.aggregation;
	}

	public LinkedHashMap<String, SelectObj> getSelectValues() {
		return selectValues;
	}

	/**
	 * Builds and returns the SELECT section of the query.
	 * 
	 * @return the SELECT section of the query
	 */
	public String buildQuerySelect(boolean ignoreAggregation) {
		StringBuilder selectStringBuilder = new StringBuilder("SELECT ");
		for (SelectObj selectable : selectValues.values()) {
			selectStringBuilder.append(selectable.toString(ignoreAggregation)).append(" ");
		}
		return selectStringBuilder.toString().trim();
	}



	class SelectObj {
		String referenceName;
		String aggregationName;
		Aggregation aggregation;


		public SelectObj(String referenceName, String aggregationName, Aggregation aggregation) {
			this.referenceName = referenceName;
			this.aggregationName = aggregationName;
			this.aggregation = aggregation;
		}

		public String toString(boolean ignoreAggregation) {
			String structure = null;
			if (ignoreAggregation) {
				structure = "?%s";
			} else {

				switch (aggregation) {
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
			return String.format(structure, referenceName, aggregationName);

		}

		@Override
		public String toString() {
			return toString(false);
		}

		@Override
		public boolean equals(Object obj) {
			if(obj instanceof SelectObj) {
				SelectObj sel = (SelectObj) obj;
				return testEqualsWithNull(sel.aggregation, aggregation) && testEqualsWithNull(sel.referenceName, referenceName)
						&& testEqualsWithNull(sel.aggregationName, aggregationName);
			} else {
				return false;
			}
		}

		private <E> boolean testEqualsWithNull(E object1, E object2) {
			if (object1 == null || object2 == null) {
				return object1 == object2;
			} else {
				return object1.equals(object2);
			}
		}
	}
}
