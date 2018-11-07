package asegroup1.api.models;

import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 * Contains the constraints to be passed
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQueryConstraint {

	private HashMap<String, Constraint> constraints;

	public LandRegistryQueryConstraint() {
		constraints = new HashMap<>();
	}

	private void addAddrConstraint(String name, String value) {
		constraints.put(name, new AddrConstraint(name, "\"" + value + "\""));
	}

	public void setPrimaryHouseName(String primaryHouseName) {
		addAddrConstraint("paon", primaryHouseName);
	}

	public void removePrimaryHouseName(String primaryHouseName) {
		constraints.remove("paon");
	}

	public void setSecondaryHouseName(String secondaryHouseName) {
		addAddrConstraint("saon", secondaryHouseName);
	}

	public void setStreetName(String streetName) {
		addAddrConstraint("street", streetName);
	}

	public void setTownName(String townName) {
		addAddrConstraint("town", townName);
	}

	public void setDistrict(String district) {
		addAddrConstraint("district", district);
	}

	public void setCounty(String county) {
		addAddrConstraint("county", county);
	}

	public void setPostCode(String postCode) throws InvalidParameterException {
		if (postCode.charAt(postCode.length() - 4) != 32) {
			throw new InvalidParameterException("Post Code: must contain a space in the correct position");
		} else {
			addAddrConstraint("postcode", postCode.toUpperCase());
		}
	}


	private void addTransConstraint(String name, String value) {
		constraints.put(name, new TransConstraint(name, value));
	}

	public void setPropertyType(PropertyType propertyType) {
		addTransConstraint("propertyType", parseEnumValue("lrcommon", propertyType.toString()));
	}

	public void setNewBuild(Boolean newBuild) {
		addTransConstraint("newBuild", newBuild.toString());
	}

	public void setEstateType(EstateType estateType) {
		addTransConstraint("estateType", parseEnumValue("lrcommon", estateType.toString()));
	}

	public void setTransactionCategory(TransactionCategory transactionCategory) {
		addTransConstraint("transactionCategory", parseEnumValue("lrppi", transactionCategory.toString()));
	}

	public String buildQueryWhere() {
		return "WHERE { \n" + buildQuerySelection() + "\n" + buildQueryColumns() + "\n}";
	}

	private String buildQuerySelection() {
		
		ArrayList<Constraint> constraintList = new ArrayList<>(constraints.values());
		// order by type
		Collections.sort(constraintList);
		
		StringBuilder whereStringBuilder = new StringBuilder();
		for(Constraint constraint : constraintList) {
			whereStringBuilder.append(constraint.toString() + " \n");
		}
		return whereStringBuilder.toString().trim();
	}

	private String buildQueryColumns() {
		return "?transx lrppi:propertyAddress ?addr ; \n" + "	lrppi:propertyType ?propertyType ; \n" + "	lrppi:estateType ?estateType ; \n"
				+ "	lrppi:transactionDate ?transactionDate ; \n" + "	lrppi:pricePaid ?pricePaid ; \n" + "	lrppi:newBuild ?newBuild ; \n"
				+ "	lrppi:transactionCategory ?transactionCategory. \n" + "OPTIONAL {?addr lrcommon:paon ?paon} \n" + "OPTIONAL {?addr lrcommon:saon ?saon} \n"
				+ "OPTIONAL {?addr lrcommon:street ?street} \n" + "OPTIONAL {?addr lrcommon:locality ?locality} \n" + "OPTIONAL {?addr lrcommon:town ?town} \n"
				+ "OPTIONAL {?addr lrcommon:district ?district} \n" + "OPTIONAL {?addr lrcommon:county ?county} \n" + "OPTIONAL {?addr lrcommon:postcode ?postcode}";
	}


	abstract class Constraint implements Comparable<Constraint> {
		String namespace, type, name, value;

		public Constraint(String namespace, String type, String name, String value) {
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

	class AddrConstraint extends Constraint {
		public AddrConstraint(String name, String value) {
			super("addr", "lrcommon", name, value);
		}

		@Override
		public int compareTo(Constraint o) {
			return o instanceof AddrConstraint ? 0 : -1;
		}
	}

	class TransConstraint extends Constraint {

		public TransConstraint(String name, String value) {
			super("transx", "lrppi", name, value);
		}

		@Override
		public int compareTo(Constraint o) {
			return o instanceof TransConstraint ? 0 : 1;
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

	private String parseEnumValue(String namespace, String enumStr) {
		return namespace + ":" + enumStr.toLowerCase().replace('_', '-');
	}

}
