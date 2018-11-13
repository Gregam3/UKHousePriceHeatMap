package asegroup1.api.models.landregistry;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import asegroup1.api.models.landregistry.LandRegistryData.EqualityConstraint;

/**
 * Contains the constraints to be passed
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQueryConstraint {

	private HashSet<RangeConstraint> rangeConstraints;
	private LandRegistryData equalityConstraints;
	private ArrayList<String> postcodes;


	public LandRegistryQueryConstraint(LandRegistryData eqalityConstraints) {
		rangeConstraints = new HashSet<>();
		this.equalityConstraints = eqalityConstraints;
		postcodes = new ArrayList<>();
	}

	public LandRegistryQueryConstraint() {
		this(new LandRegistryData());
	}



	public LandRegistryData getEqualityConstraints() {
		return equalityConstraints;
	}

	/* RANGE CONSTRAINTS */


	private void setDateConstraint(boolean isMax, LocalDate date) {
		rangeConstraints.add(new RangeConstraint("xsd:date", "transactionDate", isMax ? "<" : ">", "\"" + date.toString() + "\""));
	}

	public void setMaxDate(LocalDate date) {
		setDateConstraint(true, date);
	}

	public RangeConstraint getRangeConstraint(String name, String comparator) {
		for (RangeConstraint constraint : rangeConstraints) {
			if (constraint.getName().equals(name) && constraint.getComparator().equals(comparator)) {
				return constraint;
			}
		}
		return null;
	}

	public void setMinDate(LocalDate date) {
		setDateConstraint(false, date);
	}


	private void setPriceConstraint(boolean isMax, int price) {
		rangeConstraints.add(new RangeConstraint(null, "pricePaid", isMax ? "<" : ">", price + ""));
	}

	public void setMaxPricePaid(int price) {
		setPriceConstraint(true, price);
	}

	public void setMinPricePaid(int price) {
		setPriceConstraint(false, price);
	}



	/* QUERY GENERATION */



	public ArrayList<String> getPostcodes() {
		return postcodes;
	}

	public void setPostcodes(ArrayList<String> postcodes) {
		this.postcodes = postcodes;
	}

	public void setPostcodes(String... postcodes) {
		setPostcodes(new ArrayList<>(Arrays.asList(postcodes)));
	}

	public String buildQueryWhere() {
		String content = buildQuerySelection() + "\n" + buildQueryTransactionColumns() + "\n" + buildQueryAddressColumns() + "\n" + buildQueryFilter();
		return "WHERE { \n\t" + content.replace("\n", "\n\t") + "\n}";
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
		return "OPTIONAL {?addr lrcommon:paon ?paon} \n" + "OPTIONAL {?addr lrcommon:saon ?saon} \n"
				+ "OPTIONAL {?addr lrcommon:street ?street} \n" + "OPTIONAL {?addr lrcommon:locality ?locality} \n" + "OPTIONAL {?addr lrcommon:town ?town} \n"
				+ "OPTIONAL {?addr lrcommon:district ?district} \n" + "OPTIONAL {?addr lrcommon:county ?county} \n" + "OPTIONAL {?addr lrcommon:postcode ?postcode}";
	}

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

		@Override
		public String toString() {
			return "?" + name + " " + comparator + " " + value + (type != null ? "^^" + type : "");
		}


	}

	public enum PropertyType {
		detached, semi_detached, terraced, flat_maisonette, other;

	}

	public enum EstateType {
		freehold, leasehold;

	}

	public enum TransactionCategory {
		standard_price_paid_transaction, additional_price_paid_transaction;
	}


}
