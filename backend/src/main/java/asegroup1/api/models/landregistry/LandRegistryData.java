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
		addTransConstraint(Selectable.transactionDate, "transactionDate", date.toString(), true);
	}

	public boolean hasConstraint(Selectable selectable) {
		return constraints.containsKey(selectable);
	}

	public String getConstraint(Selectable selectable) {
		if (hasConstraint(selectable)) {
			return constraints.get(selectable).value;
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

	private String parseEnumValue(String namespace, String enumStr) {
		return namespace + ":" + enumStr.toLowerCase().replace('_', '-');
	}

	public boolean setConstraint(String name, JsonNode value) {
		Selectable selectable;
		if (value.asText().length() == 0) {
			return true;
		}
		try {
			selectable = Selectable.valueOf(name);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
			return false;
		}
		switch (selectable) {
			case county:
				setCounty(value.asText());
				return true;
			case district:
				setDistrict(value.asText());
				return true;
			case estateType:
				EstateType estateType;
				try {
					estateType = EstateType.valueOf(value.asText().toUpperCase());
					setEstateType(estateType);
					return true;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return false;
				}
			case locality:
				setLocality(value.asText());
				return true;
			case newBuild:
				if (value.isBoolean()) {
					setNewBuild(value.asBoolean());
					return true;
				} else {
					return false;
				}
			case paon:
				setPrimaryHouseName(value.asText());
				return true;
			case postcode:
				setPostCode(value.asText());
				return true;
			case pricePaid:
				if (value.isInt()) {
					setPricePaid(value.asInt());
					return true;
				} else {
					return false;
				}
			case propertyType:
				PropertyType propertyType;
				try {
					propertyType = PropertyType.valueOf(value.asText().toUpperCase().replace("-", "_"));
					setPropertyType(propertyType);
					return true;
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return false;
				}
			case saon:
				setSecondaryHouseName(value.asText());
				return true;
			case street:
				setStreetName(value.asText());
				return true;
			case town:
				setTownName(value.asText());
				return true;
			case transactionCategory:
				TransactionCategory transactionCategory;
				try {
					transactionCategory = TransactionCategory.valueOf(value.asText().toUpperCase().replace(" ", "_"));
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
					return false;
				}
				setTransactionCategory(transactionCategory);
				return true;
			case transactionDate:
				try {
					LocalDate date = LocalDate.parse(value.asText());
					setTransactionDate(date);
					return true;
				} catch (DateTimeParseException e) {
					e.printStackTrace();
					return false;
				}
		}
		return false;
	}

	public boolean parseResponse(JsonNode json) {
		boolean sucessful = true;

		Iterator<Entry<String, JsonNode>> iter = json.fields();
		while (iter.hasNext()) {
			Entry<String, JsonNode> field = iter.next();
			if (field.getValue().has("value")) {
				if (!setConstraint(field.getKey(), field.getValue().get("value"))) {
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
		private String namespace, type, name, value;
		private boolean isString;

		public EqualityConstraint(String namespace, String type, String name, String value, boolean isString) {
			this.namespace = namespace;
			this.type = type;
			this.name = name;
			this.value = value;
			this.isString = isString;
		}

		public String toString() {
			return getNamespace() + " " + getSelector() + " " + (isString ? "\"" + value + "\"" : getvalue()) + ".";
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
	}
}
