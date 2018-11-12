package asegroup1.api.models.landregistry;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;

import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.EstateType;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.PropertyType;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.TransactionCategory;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable;

public class LandRegistryData {
	private HashMap<Selectable, EqualityConstraint> constraints;

	public LandRegistryData() {
		constraints = new HashMap<>();
	}

	public LandRegistryData(JsonNode json) {
		constraints = new HashMap<>();
		parseResponse(json);
	}


	private void addAddrConstraint(Selectable selectable, String value) {
		constraints.put(selectable, new AddrConstraint(selectable.toString(), value.toUpperCase()));
	}

	public void setPrimaryHouseName(String primaryHouseName) {
		addAddrConstraint(Selectable.paon, primaryHouseName);
	}

	public void setSecondaryHouseName(String secondaryHouseName) {
		addAddrConstraint(Selectable.saon, secondaryHouseName);
	}

	public void setStreetName(String streetName) {
		addAddrConstraint(Selectable.street, streetName);
	}

	public void setTownName(String townName) {
		addAddrConstraint(Selectable.town, townName);
	}

	public void setLocality(String locality) {
		addAddrConstraint(Selectable.locality, locality);
	}

	public void setDistrict(String district) {
		addAddrConstraint(Selectable.district, district);
	}

	public void setCounty(String county) {
		addAddrConstraint(Selectable.county, county);
	}

	public void setPostCode(String postCode) throws InvalidParameterException {
		if (postCode.charAt(postCode.length() - 4) != 32) {
			throw new InvalidParameterException("Post Code: must contain a space in the correct position");
		} else {
			addAddrConstraint(Selectable.postcode, postCode);
		}
	}

	private void addTransConstraint(Selectable selectable, String name, String value, boolean isString) {
		constraints.put(selectable, new TransConstraint(name, value, isString));
	}

	private void addTransConstraint(Selectable selectable, String name, String value) {
		addTransConstraint(selectable, name, value, false);
	}


	public void setPropertyType(PropertyType propertyType) {
		addTransConstraint(Selectable.propertyType, "propertyType/skos:prefLabel", parseEnumAsString("lrcommon", propertyType.toString()));
	}

	public void setNewBuild(Boolean newBuild) {
		addTransConstraint(Selectable.newBuild, "newBuild", newBuild.toString());
	}

	public void setEstateType(EstateType estateType) {
		addTransConstraint(Selectable.estateType, "estateType/skos:prefLabel", parseEnumAsString("lrcommon", estateType.toString()));
	}

	public void setTransactionCategory(TransactionCategory transactionCategory) {
		addTransConstraint(Selectable.transactionCategory, "transactionCategory/skos:prefLabel", parseEnumAsString("lrppi", transactionCategory.toString()));
	}

	public void setPricePaid(int pricePaid) {
		addTransConstraint(Selectable.pricePaid, "pricePaid", pricePaid + "");
	}

	public void setTransactionDate(LocalDate date) {
		addTransConstraint(Selectable.transactionDate, "transactionDate", date.toString(), true);
	}

	public boolean hasConstraint(Selectable selectable) {
		return constraints.containsKey(selectable);
	}

	public String getConstraint(Selectable selectable) {
		if (hasConstraint(selectable)) {
			return constraints.get(selectable).getvalue();
		} else {
			return null;
		}
	}

	public boolean removeConstraint(Selectable selectable) {
		if (hasConstraint(selectable)) {
			constraints.remove(selectable);
			return true;
		} else {
			return false;
		}

	}

	private String parseEnumAsString(String namespace, String enumStr) {
		return namespace + ":" + enumStr.replace('_', '-');
	}

	public boolean setConstraint(String name, String value) {
		Selectable selectable;
		if (value.length() == 0) {
			return true;
		}
		if (name.length() < 2) {
			return false;
		}
		try {
			selectable = Selectable.valueOf(name.substring(0, 1).toLowerCase() + name.substring(1));
		} catch (IllegalArgumentException e) {
			return false;
		}
		switch (selectable) {
			case county:
				setCounty(value);
				return true;
			case district:
				setDistrict(value);
				return true;
			case estateType:
				EstateType estateType;
				try {
					estateType = EstateType.valueOf(value);
					setEstateType(estateType);
					return true;
				} catch (IllegalArgumentException e) {
					return false;
				}
			case locality:
				setLocality(value);
				return true;
			case newBuild:
				if (value.toLowerCase().matches("(false)|(true)")) {
					setNewBuild(Boolean.parseBoolean(value));
					return true;
				} else {
					return false;
				}
			case paon:
				setPrimaryHouseName(value);
				return true;
			case postcode:
				setPostCode(value);
				return true;
			case pricePaid:
				try {
					setPricePaid(Integer.parseInt(value));
					return true;
				} catch (NumberFormatException e) {
					return false;
				}
			case propertyType:
				PropertyType propertyType;
				try {
					propertyType = PropertyType.valueOf(value);
					setPropertyType(propertyType);
					return true;
				} catch (IllegalArgumentException e) {
					return false;
				}
			case saon:
				setSecondaryHouseName(value);
				return true;
			case street:
				setStreetName(value);
				return true;
			case town:
				setTownName(value);
				return true;
			case transactionCategory:
				TransactionCategory transactionCategory;
				try {
					transactionCategory = TransactionCategory.valueOf(value);
				} catch (IllegalArgumentException e) {
					return false;
				}
				setTransactionCategory(transactionCategory);
				return true;
			case transactionDate:
				try {
					LocalDate date = LocalDate.parse(value);
					setTransactionDate(date);
					return true;
				} catch (DateTimeParseException e) {
					return false;
				}
			default:
				return false;
		}
	}

	public boolean parseResponse(JsonNode json) {
		boolean sucessful = true;

		Iterator<Entry<String, JsonNode>> iter = json.fields();
		while (iter.hasNext()) {
			Entry<String, JsonNode> field = iter.next();
			if (field.getValue().has("value")) {
				if (!setConstraint(field.getKey(), field.getValue().get("value").asText())) {
					sucessful = false;
				}
			}
		}
		return sucessful;
	}

	@JsonIgnore
	public HashMap<Selectable, EqualityConstraint> getAllConstraints() {
		return constraints;
	}

	public HashMap<String, String> getMappings() {
		HashMap<String, String> retMap = new HashMap<>();
		constraints.forEach((k, v) -> retMap.put(k.toString(), v.getvalue()));
		return retMap;
	}


	abstract class EqualityConstraint implements Comparable<EqualityConstraint> {
		protected String namespace, type, name, value;
		private boolean isString;

		public EqualityConstraint(String namespace, String type, String name, String value, boolean isString) {
			this.namespace = namespace;
			this.type = type;
			this.name = name;
			this.value = value;
			this.isString = isString;
		}

		public String toString() {
			return getNamespace() + " " + getSelector() + " " + (isString ? "\"" + value + "\"" : value) + ".";
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
			super("addr", "lrcommon", name, value, true);
		}

		@Override
		public int compareTo(EqualityConstraint o) {
			return o instanceof AddrConstraint ? 0 : -1;
		}
	}

	class TransConstraint extends EqualityConstraint {

		public TransConstraint(String name, String value, boolean isString) {
			super("transx", "lrppi", name, value, isString);
		}

		@Override
		public int compareTo(EqualityConstraint o) {
			return o instanceof TransConstraint ? 0 : 1;
		}

		@Override
		public String getvalue() {
			if (value.contains(":")) {
				return value.substring(value.indexOf(':') + 1).replace('-', '_');
			} else {
				return value;
			}
		}
	}
}
