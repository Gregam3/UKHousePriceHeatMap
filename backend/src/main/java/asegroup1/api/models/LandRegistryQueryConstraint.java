package asegroup1.api.models;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;

import asegroup1.api.models.LandRegistryQuerySelect.Selectable;

/**
 * Contains the constraints to be passed
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQueryConstraint {

	private HashMap<Selectable, EqualityConstraint> equalityConstraints;
	private HashSet<RangeConstraint> rangeConstraints;


	public LandRegistryQueryConstraint() {
		equalityConstraints = new HashMap<>();
		rangeConstraints = new HashSet<>();
	}

	/* EQUALITY CONSTRAINTS */

	private void addAddrConstraint(Selectable selectable, String name, String value) {
		equalityConstraints.put(selectable, new AddrConstraint(name, "\"" + value.toUpperCase() + "\""));
	}

	public void setPrimaryHouseName(String primaryHouseName) {
		addAddrConstraint(Selectable.primaryAddress, "paon", primaryHouseName);
	}

	public void setSecondaryHouseName(String secondaryHouseName) {
		addAddrConstraint(Selectable.secondaryAddress, "saon", secondaryHouseName);
	}

	public void setStreetName(String streetName) {
		addAddrConstraint(Selectable.street, "street", streetName);
	}

	public void setTownName(String townName) {
		addAddrConstraint(Selectable.town, "town", townName);
	}

	public void setLocality(String locality) {
		addAddrConstraint(Selectable.locality, "locality", locality);
	}

	public void setDistrict(String district) {
		addAddrConstraint(Selectable.district, "district", district);
	}

	public void setCounty(String county) {
		addAddrConstraint(Selectable.county, "county", county);
	}

	public void setPostCode(String postCode) throws InvalidParameterException {
		if (postCode.charAt(postCode.length() - 4) != 32) {
			throw new InvalidParameterException("Post Code: must contain a space in the correct position");
		} else {
			addAddrConstraint(Selectable.postcode, "postcode", postCode);
		}
	}

	private void addTransConstraint(Selectable selectable, String name, String value) {
		equalityConstraints.put(selectable, new TransConstraint(name, value));
	}

	public void setPropertyType(PropertyType propertyType) {
		addTransConstraint(Selectable.propertyType, "propertyType/skos:prefLabel", parseEnumValue("lrcommon", propertyType.toString()));
	}

	public void setNewBuild(Boolean newBuild) {
		addTransConstraint(Selectable.newBuild, "newBuild", newBuild.toString());
	}

	public void setEstateType(EstateType estateType) {
		addTransConstraint(Selectable.estateType, "estateType/skos:prefLabel", parseEnumValue("lrcommon", estateType.toString()));
	}

	public void setTransactionCategory(TransactionCategory transactionCategory) {
		addTransConstraint(Selectable.transactionCategory, "transactionCategory/skos:prefLabel", parseEnumValue("lrppi", transactionCategory.toString()));
	}

	public void setPricePaid(int pricePaid) {
		addTransConstraint(Selectable.pricePaid, "pricePaid", pricePaid + "");
	}

	public void setTransactionDate(LocalDate date) {
		addTransConstraint(Selectable.transactionDate, "transactionDate", "\"" + date.toString() + "\"");
	}

	public boolean hasConstraint(Selectable selectable) {
		return equalityConstraints.containsKey(selectable);
	}

	public String getConstraint(Selectable selectable) {
		if (hasConstraint(selectable)) {
			return equalityConstraints.get(selectable).value;
		} else {
			return null;
		}
	}

	public boolean removeConstraint(Selectable selectable) {
		if (hasConstraint(selectable)) {
			equalityConstraints.remove(selectable);
			return true;
		} else {
			return false;
		}

	}

	private String parseEnumValue(String namespace, String enumStr) {
		return namespace + ":" + enumStr.toLowerCase().replace('_', '-');
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
		
		ArrayList<EqualityConstraint> constraintList = new ArrayList<>(equalityConstraints.values());
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

	abstract class EqualityConstraint implements Comparable<EqualityConstraint> {
		private String namespace, type, name, value;

		public EqualityConstraint(String namespace, String type, String name, String value) {
			this.namespace = namespace;
			this.type = type;
			this.name = name;
			this.value = value;
		}

		public String toString() {
			return getNamespace() + " " + getSelector() + " " + getvalue() + ".";
		}

		public String getNamespace() {
			return "?" + namespace;
		}

		public String getSelector() {
			return type + ":" + name;
		}

		public String getvalue() {
			return value;
		}
	}

	class AddrConstraint extends EqualityConstraint {
		public AddrConstraint(String name, String value) {
			super("addr", "lrcommon", name, value);
		}

		@Override
		public int compareTo(EqualityConstraint o) {
			return o instanceof AddrConstraint ? 0 : -1;
		}
	}

	class TransConstraint extends EqualityConstraint {

		public TransConstraint(String name, String value) {
			super("transx", "lrppi", name, value);
		}

		@Override
		public int compareTo(EqualityConstraint o) {
			return o instanceof TransConstraint ? 0 : 1;
		}
	}


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
		DETATCHED, SEMI_DETACHED, TERRACED, FLAT, OTHER;
	}

	public enum EstateType {
		FREEHOLD, LEASEHOLD;
	}

	public enum TransactionCategory {
		STANDARD_PRICE_PAID_TRANSACTION, ADDITIONAL_PRICE_PAID_TRANSACTION;
	}

}
