package asegroup1.api.models.landregistry;

import asegroup1.api.controllers.LandRegistryController;
import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.landregistry.LandRegistryQuery.EstateType;
import asegroup1.api.models.landregistry.LandRegistryQuery.PropertyType;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQuery.TransactionCategory;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.JsonNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class LandRegistryData implements Comparable{
    private final static Logger logger = LogManager.getLogger(LandRegistryController.class);

    private HashMap<Selectable, EqualityConstraint> constraints;

    private Double latitude, longitude;

    private String id;

    public static final int YEARS_TO_FETCH = 5;

    private Colour colour;

	private Double radius;

    /**
     * Initialise the {@Link LandRegistryData} class, to be empty
     */
    public LandRegistryData() {
        constraints = new HashMap<>();
        longitude = null;
        latitude = null;
        id = UUID.randomUUID().toString();
		radius = null;
    }

    /**
     * Initialise the {@Link LandRegistryData} class, with the content of a
     * {@link JsonNode}. The {@link JsonNode} is sent to
     * {@link #parseResponse(JsonNode)}.
     *
     * @param json fields to initialise the class with.
     */
    public LandRegistryData(JsonNode json) {
        this();
        parseResponse(json);
    }

    public void setColour(Colour colour) {
        this.colour = colour;
    }

    public Colour getColour() {
        return colour;
    }

    public String getId() {
        return id;
    }

    private void addAddrConstraint(Selectable selectable, String value, boolean isString) {
		addAddrConstraint(selectable, value, isString, true);
	}

	private void addAddrConstraint(Selectable selectable, String value, boolean isString, boolean isUpperCase) {
		constraints.put(selectable, new AddrConstraint(selectable.toString(), isUpperCase ? value.toUpperCase() : value, isString));
    }

    private void addAddrConstraint(Selectable selectable, String value) {
        addAddrConstraint(selectable, value, true);
    }


    /**
     * Set the primaryHouseName
     *
     * @param primaryHouseName to set
     */
    public void setPrimaryHouseName(String primaryHouseName) {
        addAddrConstraint(Selectable.paon, primaryHouseName);
    }

    /**
     * Set the secondaryHouseName
     *
     * @param secondaryHouseName to set
     */
    public void setSecondaryHouseName(String secondaryHouseName) {
        addAddrConstraint(Selectable.saon, secondaryHouseName);
    }

    /**
     * Set the streetName
     *
     * @param streetName to set
     */
    public void setStreetName(String streetName) {
        addAddrConstraint(Selectable.street, streetName);
    }

    /**
     * Set the townName
     *
     * @param townName to set
     */
    public void setTownName(String townName) {
        addAddrConstraint(Selectable.town, townName);
    }

    /**
     * Set the locality
     *
     * @param locality to set
     */
    public void setLocality(String locality) {
        addAddrConstraint(Selectable.locality, locality);
    }

    /**
     * Set the district
     *
     * @param district to set
     */
    public void setDistrict(String district) {
        addAddrConstraint(Selectable.district, district);
    }

    /**
     * Set the county
     *
     * @param county to set
     */
    public void setCounty(String county) {
        addAddrConstraint(Selectable.county, county);
    }

    /**
	 * Set the postCode. The post code must be in the form "A(A)9(A|9) 9AA" where A
	 * is a letter, and 9 is a digit, and (A) denotes an optional character.
	 *
	 * @param postCode to set
	 * @throws InvalidParameterException if the post code is invalid
	 */
    public void setPostCode(String postCode) throws InvalidParameterException {
		if (postCode == null || postCode.length() < 7 || !postCode.matches("[A-Za-z]{1,2}[0-9][0-9A-Za-z]?[ ][0-9][A-Za-z]{2}")) {
			throw new InvalidParameterException("Invalid ParaException");
		}
		addAddrConstraint(Selectable.postcode, postCode);
    }

    private void addTransConstraint(Selectable selectable, String name, String value, boolean isString) {
        constraints.put(selectable, new TransConstraint(name, value, isString));
    }

    private void addTransConstraint(Selectable selectable, String name, String value) {
        addTransConstraint(selectable, name, value, false);
    }


    /**
     * Set the propertyType
     *
     * @param propertyType to set
     */
    public void setPropertyType(PropertyType propertyType) {
        addTransConstraint(Selectable.propertyType, "propertyType/skos:prefLabel", parseEnumAsString("lrcommon", propertyType.toString()));
    }

    /**
     * Set if the property was newly built
     *
     * @param newBuild to set
     */
    public void setNewBuild(Boolean newBuild) {
        addTransConstraint(Selectable.newBuild, "newBuild", newBuild.toString());
    }

    /**
     * Set the estateType
     *
     * @param estateType to set
     */
    public void setEstateType(EstateType estateType) {
        addTransConstraint(Selectable.estateType, "estateType/skos:prefLabel", parseEnumAsString("lrcommon", estateType.toString()));
    }

    /**
     * Set the transactionCategory
     *
     * @param transactionCategory to set
     */
    public void setTransactionCategory(TransactionCategory transactionCategory) {
        addTransConstraint(Selectable.transactionCategory, "transactionCategory/skos:prefLabel", parseEnumAsString("lrppi", transactionCategory.toString()));
    }

    /**
     * Set the pricePaid
     *
     * @param pricePaid to set
     */
    public void setPricePaid(long pricePaid) {
        addTransConstraint(Selectable.pricePaid, "pricePaid", pricePaid + "");
    }

    /**
     * Set the transaction date
     *
     * @param date to set
     */
    public void setTransactionDate(LocalDate date) {
        addTransConstraint(Selectable.transactionDate, "transactionDate", date.toString(), true);
    }


    /**
     * Check if a constraint is mapped to a value.
     *
     * @param selectable to check
     * @return true, if the specified {@link Selectable} exists with a value in this
     * instance.
     */
    public boolean hasConstraint(Selectable selectable) {
        return constraints.containsKey(selectable);
    }

    /**
     * Get the value of a constraint stored in this instance.
     *
     * @param selectable to get the value of
     * @return a string representing the value of the {@link Selectable} stored, or
     * null if no such value is stored.
     */
    public String getConstraint(Selectable selectable) {
        if (hasConstraint(selectable)) {
            return constraints.get(selectable).getValue();
        } else {
            return null;
        }
    }

    /**
     * Get the value of a constraint stored in this instance.
     *
     * @param selectable to get the value of
     * @return a string representing the value of the {@link Selectable} stored, or
     * an empty string if no such value is stored.
     */
    public String getConstraintNotNull(Selectable selectable) {
        String constraint = getConstraint(selectable);
        return constraint == null ? "" : constraint;
    }

    /**
     * Remove any value mapped to a constraint in this instance.
     *
     * @param selectable to remove.
     * @return true, if a mapping existed, false otherwise.
     */
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

    /**
     * Attempts to set a constraint by parsing the name of the constraint and its
     * value. Provided both are valid, they are stored in this object.
     *
     * @param name  of the {@link Selectable} to be set
     * @param value to set the selectable to
     * @return true, if the value was added successfully
     * @throws IllegalArgumentException if the name is invalid
     */
    public boolean setConstraint(String name, String value) {
        Selectable selectable;

        try {
            if (name.length() < 2) {
                throw new IllegalArgumentException("Constraint Name is Invalid");
            }
            selectable = Selectable.valueOf(name.substring(0, 1).toLowerCase() + name.substring(1));
        } catch (IllegalArgumentException e) {
            logger.error("Unable to set constraint value", e);
            throw new IllegalArgumentException("Constraint Name is Invalid");
        }
        if (value.length() == 0) {
            removeConstraint(selectable);
            return true;
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
                    setPricePaid(Math.round(Double.parseDouble(value)));
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
				throw new IllegalArgumentException("Unknown selectable \"" + name + "\"");
        }
    }

	EqualityConstraint getEqualityConstraint(Selectable s) {
		return constraints.get(s);
	}

	void setConstraintVar(Selectable constraint, String variableReference) {
		variableReference = "?" + variableReference;

		switch (constraint) {
			case paon:
			case saon:
			case street:
			case locality:
			case town:
			case district:
			case county:
			case postcode:
			case transactionDate:
				addAddrConstraint(constraint, variableReference, false, false);
				break;
			case propertyType:
			case estateType:
				addTransConstraint(constraint, constraint.toString() + "/skos:prefLabel", "lrcommon:" + variableReference);
				break;
			case transactionCategory:
				addTransConstraint(constraint, constraint.toString() + "/skos:prefLabel", "lrppi:" + variableReference);
				break;
			case newBuild:
			case pricePaid:
				addTransConstraint(constraint, constraint.toString(), variableReference);
				break;
			default:
				throw new IllegalArgumentException("Unexpected enum");
		}
	}

	static List<String> processConstraintList(Selectable type, String... constraints) {
        switch (type) {
            case paon:
            case saon:
            case street:
            case locality:
            case town:
            case district:
            case county:
            case postcode:
            case transactionDate:
				return Arrays.asList(constraints).stream().map(v -> "\"" + v.toUpperCase() + "\"").collect(Collectors.toList());
            case propertyType:
            case estateType:
                return Arrays.asList(constraints).stream().map(v -> "lrcommon:" + v).collect(Collectors.toList());
            case transactionCategory:
                return Arrays.asList(constraints).stream().map(v -> "lrppi:" + v).collect(Collectors.toList());
            case newBuild:
            case pricePaid:
                return Arrays.asList(constraints);
            default:
                throw new IllegalArgumentException("Unexpected enum");
        }
    }

    /**
     * Parses the JSON, and fills its self with the content.
     *
     * @param json to fill this instance with
     * @return true, if the JSON was parsed without error.
     */
    public boolean parseResponse(JsonNode json) {
        boolean successful = true;

        Iterator<Entry<String, JsonNode>> iter = json.fields();
        while (iter.hasNext()) {
            Entry<String, JsonNode> field = iter.next();
            if (field.getValue().has("value")) {
                if (!setConstraint(field.getKey(), field.getValue().get("value").asText())) {
                    successful = false;
                }
            }
        }
        return successful;
    }

    /**
     * Get the mappings stored in this instance
     *
     * @return the mappings stored in this instance
     */
    @JsonIgnore
    public HashMap<Selectable, EqualityConstraint> getAllConstraints() {
        return constraints;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof LandRegistryData) {
            LandRegistryData data = (LandRegistryData) obj;
            if (data.getAllConstraints().size() != getAllConstraints().size()) {
                return false;
            } else {
                return data.getAllConstraints().equals(getAllConstraints());
            }
        } else {
            return false;
        }
    }

    /**
     * Get the latitude.
     *
     * @return the latitude
     */

    @JsonIgnore
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Set the latitude.
     *
     * @param latitude to set
     */
    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    /**
     * Get the longitude.
     *
     * @return the longitude
     */
    @JsonIgnore
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Set the longitude.
     *
     * @param longitude to set
     */
    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

	@Override
	public int compareTo(Object obj) {
		if (obj instanceof LandRegistryData) {
			long thisPricePaid = Long.valueOf(this.getConstraint(Selectable.pricePaid));
			long thatPricePaid = Long.valueOf(((LandRegistryData) obj).getConstraint(Selectable.pricePaid));

			return Long.compare(thisPricePaid, thatPricePaid);
		} else {
			return -1;
		}
	}

	@JsonIgnore
	public Double getRadius() {
		return radius;
	}

	public void setRadius(Double radius) {
		this.radius = radius;
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

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof EqualityConstraint) {
                EqualityConstraint constraint = (EqualityConstraint) obj;
                return this.toString().equals(constraint.toString());
            } else {
                return false;
            }
        }
    }

    class AddrConstraint extends EqualityConstraint {
        public AddrConstraint(String name, String value, boolean string) {
            super("addr", "lrcommon", name, value, string);
        }

        @Override
        public int compareTo(EqualityConstraint o) {
			return o instanceof AddrConstraint ? (equals(o) ? 0 : 1) : -1;
        }
    }

    class TransConstraint extends EqualityConstraint {

        public TransConstraint(String name, String value, boolean isString) {
            super("transx", "lrppi", name, value, isString);
        }

        @Override
        public int compareTo(EqualityConstraint o) {
			return o instanceof TransConstraint ? (equals(o) ? 0 : -1) : 1;
        }

        @Override
        public String getValue() {
            if (value.contains(":")) {
                return value.substring(value.indexOf(':') + 1).replace('-', '_');
            } else {
                return value;
            }
        }
    }

}
