package asegroup1.api.models.landregistry;

import java.time.LocalDate;
import java.util.ArrayList;
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

	public LandRegistryQueryConstraint(LandRegistryData eqalityConstraints) {
		rangeConstraints = new HashSet<>();
		this.equalityConstraints = eqalityConstraints;
	}

	public LandRegistryQueryConstraint() {
		rangeConstraints = new HashSet<>();
		equalityConstraints = new LandRegistryData();
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

	public String buildQueryWhere() {
		String content = buildQuerySelection() + "\n" + buildQueryTransactionColumns() + buildQueryFilter() + "\n" + buildQueryAddressColumns();
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
		if (rangeConstraints.isEmpty()) {
			return ".";
		} else {
			StringBuilder filterStringBuilder = new StringBuilder("\nFILTER (\n\t");
			int i = rangeConstraints.size();
			for (RangeConstraint constraint : rangeConstraints) {
				filterStringBuilder.append(constraint.toString());
				if (--i != 0) {
					filterStringBuilder.append(" &&\n\t");
				} else {
					filterStringBuilder.append("\n");
				}
			}
			filterStringBuilder.append(")");
			return filterStringBuilder.toString();
		}
	}

	private String buildQueryTransactionColumns() {
		return "?transx lrppi:propertyAddress ?addr ; \n" + "	lrppi:propertyType/skos:prefLabel ?propertyType ; \n" + "	lrppi:estateType/skos:prefLabel ?estateType ; \n"
				+ "	lrppi:transactionDate ?transactionDate ; \n" + "	lrppi:pricePaid ?pricePaid ; \n" + "	lrppi:newBuild ?newBuild ; \n"
				+ "	lrppi:transactionCategory/skos:prefLabel ?transactionCategory";
	}

	private String buildQueryAddressColumns() {
		return "OPTIONAL {?addr lrcommon:paon ?paon} \n" + "OPTIONAL {?addr lrcommon:saon ?saon} \n"
				+ "OPTIONAL {?addr lrcommon:street ?street} \n" + "OPTIONAL {?addr lrcommon:locality ?locality} \n" + "OPTIONAL {?addr lrcommon:town ?town} \n"
				+ "OPTIONAL {?addr lrcommon:district ?district} \n" + "OPTIONAL {?addr lrcommon:county ?county} \n" + "OPTIONAL {?addr lrcommon:postcode ?postcode}";
	}

	/* TYPE DECLARATIONS */




	class RangeConstraint {
		private String type, name, comparitor, value;

		public RangeConstraint(String type, String name, String comparitor, String value) {
			this.type = type;
			this.name = name;
			this.comparitor = comparitor;
			this.value = value;
		}

		public String getType() {
			return type;
		}

		public String getName() {
			return name;
		}

		public String getComparitor() {
			return comparitor;
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
				return constraint.getComparitor().equals(getComparitor()) && constraint.getName().equals(getName()) && constraint.getType().equals(getType());
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return "?" + name + " " + comparitor + " " + value + (type != null ? "^^" + type : "");
		}


	}

	public enum PropertyType {
		DETACHED, SEMI_DETACHED, TERRACED, FLAT_MAISONETTE, OTHER;
	}

	public enum EstateType {
		FREEHOLD, LEASEHOLD;
	}

	public enum TransactionCategory {
		STANDARD_PRICE_PAID_TRANSACTION, ADDITIONAL_PRICE_PAID_TRANSACTION;
	}

}
