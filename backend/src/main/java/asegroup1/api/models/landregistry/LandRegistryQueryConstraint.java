package asegroup1.api.models.landregistry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import asegroup1.api.models.landregistry.LandRegistryData.EqualityConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

/**
 * Contains the constraints to be passed
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQueryConstraint implements LandRegistryQueryBody {

	private HashSet<RangeConstraint> rangeConstraints;
	private HashMap<String, LandRegistryQueryValues> values;
	private LandRegistryData equalityConstraints;
	private ArrayList<String> postcodes;


	/**
	 * Initialise the LandRegistryQueryConstraint with equality Constraints
	 * 
	 * @param equalityConstraints to initialise the instance with
	 */
	public LandRegistryQueryConstraint(LandRegistryData equalityConstraints) {
		rangeConstraints = new HashSet<>();
		this.equalityConstraints = equalityConstraints;
		postcodes = new ArrayList<>();
		values = new HashMap<String, LandRegistryQueryValues>();
	}

	/**
	 * Initialise the LandRegistryQueryConstraint to be empty
	 */
	public LandRegistryQueryConstraint() {
		this(new LandRegistryData());
	}

	/**
	 * Get the equality constraints stored in this instance.
	 * 
	 * @return the equality constraints stored in this instance
	 */
	public LandRegistryData getEqualityConstraints() {
		return equalityConstraints;
	}

	/* EQUALITY CONSTRAINTS */
	public void setEqualityConstraint(Selectable name, String... constraints) {
		if (constraints.length == 1) {
			getEqualityConstraints().setConstraint(name.toString(), constraints[0]);
		} else {
			String varName = name.toString().toUpperCase() + "CONSTRAINTS";
			LandRegistryQueryValues value = new LandRegistryQueryValues(varName, LandRegistryData.processConstraintList(name, constraints));
			values.put(name.toString(), value);
			getEqualityConstraints().setConstraintVar(name, varName);
		}
	}



	/* RANGE CONSTRAINTS */


	private void setDateConstraint(boolean isMax, LocalDate date) {
		rangeConstraints.add(new RangeConstraint("xsd:date", "transactionDate", isMax ? "<" : ">", "\"" + date.toString() + "\""));
	}

	/**
	 * Get a range constraint stored in this instance.
	 * 
	 * @param name       of the variable that is being constrained
	 * @param comparator that is being used to constrain the variable
	 * @return the constraint if one exists in the instance
	 */
	public RangeConstraint getRangeConstraint(String name, String comparator) {
		for (RangeConstraint constraint : rangeConstraints) {
			if (constraint.getName().equals(name) && constraint.getComparator().equals(comparator)) {
				return constraint;
			}
		}
		return null;
	}

	/**
	 * Set the maximum date that entries can have.
	 * 
	 * @param date to set
	 */
	public void setMaxDate(LocalDate date) {
		setDateConstraint(true, date);
	}

	/**
	 * Set the minimum date that entries can have.
	 * 
	 * @param date to set
	 */
	public void setMinDate(LocalDate date) {
		setDateConstraint(false, date);
	}


	private void setPriceConstraint(boolean isMax, int price) {
		rangeConstraints.add(new RangeConstraint(null, "pricePaid", isMax ? "<" : ">", price + ""));
	}

	/**
	 * Set the maximum transaction price that entries can have.
	 * 
	 * @param price to set
	 */
	public void setMaxPricePaid(int price) {
		setPriceConstraint(true, price);
	}

	/**
	 * Set the minimum transaction price that entries can have.
	 * 
	 * @param price to set
	 */
	public void setMinPricePaid(int price) {
		setPriceConstraint(false, price);
	}



	/* POSTCODE */


	/**
	 * Get all post codes that entries can have.
	 * 
	 * @return all post codes that entries can have
	 */
	public ArrayList<String> getPostcodes() {
		return postcodes;
	}

	/**
	 * Set all post codes that entries may have.
	 * 
	 * @param postcodes to set
	 */
	public void setPostcodeRegex(ArrayList<String> postcodes) {
		this.postcodes = postcodes;
	}

	/**
	 * Set all post codes that entries may have.
	 * 
	 * @param postcodes to set
	 */
	public void setPostcodeRegex(String... postcodes) {
		setPostcodeRegex(new ArrayList<>(Arrays.asList(postcodes)));
	}

	/* QUERY GENERATION */

	/**
	 * Builds and returns the WHERE section of the query.
	 * 
	 * @return the WHERE section of the query
	 */
	public String buildQueryContent() {
		return buildQueryValues() + "\n" + buildQuerySelection() + "\n" + buildQueryTransactionColumns() + "\n" + buildQueryAddressColumns() + "\n" + buildQueryFilter();
	}

	private String buildQueryValues() {
		StringBuilder valuesBuilder = new StringBuilder();
		for (LandRegistryQueryValues value : values.values()) {
			valuesBuilder.append(value.toString()).append("\n");
		}
		return valuesBuilder.toString().trim();
	}

	private String buildQuerySelection() {

		ArrayList<EqualityConstraint> constraintList = new ArrayList<>(equalityConstraints.getAllConstraints().values());
		// order by type
		Collections.sort(constraintList);

		StringBuilder whereStringBuilder = new StringBuilder();
		for (EqualityConstraint constraint : constraintList) {
			whereStringBuilder.append(constraint.toString() + " \n");
		}
		return whereStringBuilder.toString().trim();
	}

	private String buildQueryFilter() {
		boolean hasPoscodes = postcodes != null && !postcodes.isEmpty();
		if (rangeConstraints.isEmpty() && !hasPoscodes) {
			return "";
		} else {
			StringBuilder filterStringBuilder = new StringBuilder("\nFILTER (\n\t");
			int i = rangeConstraints.size();
			for (RangeConstraint constraint : rangeConstraints) {
				filterStringBuilder.append(constraint.toString());
				if (hasPoscodes || --i != 0) {
					filterStringBuilder.append(" &&\n\t");
				} else {
					filterStringBuilder.append("\n");
				}
			}
			if (hasPoscodes) {
				filterStringBuilder.append("REGEX(?postcode, \"");
				for (i = 0; i < postcodes.size(); i++) {
					filterStringBuilder.append("(" + postcodes.get(i).toUpperCase() + ")");
					if (i != postcodes.size() - 1) {
						filterStringBuilder.append("|");
					}
				}
				filterStringBuilder.append("\")\n");
			}

			filterStringBuilder.append(")");
			return filterStringBuilder.toString();
		}
	}

	private String buildQueryTransactionColumns() {
		return "?transx lrppi:propertyAddress ?addr ; \n" + "	lrppi:propertyType/skos:prefLabel ?propertyType ; \n" + "	lrppi:estateType/skos:prefLabel ?estateType ; \n"
				+ "	lrppi:transactionDate ?transactionDate ; \n" + "	lrppi:pricePaid ?pricePaid ; \n" + "	lrppi:newBuild ?newBuild ; \n"
				+ "	lrppi:transactionCategory/skos:prefLabel ?transactionCategory.";
	}

	private String buildQueryAddressColumns() {
		return "OPTIONAL {?addr lrcommon:paon ?paon} \n" + "OPTIONAL {?addr lrcommon:saon ?saon} \n" + "OPTIONAL {?addr lrcommon:street ?street} \n"
				+ "OPTIONAL {?addr lrcommon:locality ?locality} \n" + "OPTIONAL {?addr lrcommon:town ?town} \n" + "OPTIONAL {?addr lrcommon:district ?district} \n"
				+ "OPTIONAL {?addr lrcommon:county ?county} \n" + "OPTIONAL {?addr lrcommon:postcode ?postcode}";
	}

	/**
	 * Builds and returns the GROUP section of the query.
	 * 
	 * @return the GROUP section of the query
	 */
	public String buildUniqueGrouping() {
		return "GROUP BY ?paon ?saon ?street ?postcode";
	}

	/* TYPE DECLARATIONS */

	class RangeConstraint {
		private String type, name, comparator, value;

		public RangeConstraint(String type, String name, String comparator, String value) {
			this.type = type;
			this.name = name;
			this.comparator = comparator;
			this.value = value;
		}

		public String getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public String getComparator() {
			return comparator;
		}

		public String getValue() {
			return value;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(Object obj) {
			if (obj instanceof RangeConstraint) {
				RangeConstraint constraint = (RangeConstraint) obj;
				// value is not compared, to force only once instance of a specific constraint
				// in the rangeConstraints list
				return constraint.getComparator().equals(getComparator()) && constraint.getName().equals(getName()) && constraint.getType().equals(getType());
			} else {
				return false;
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return "?" + name + " " + comparator + " " + value + (type != null ? "^^" + type : "");
		}


	}

}
