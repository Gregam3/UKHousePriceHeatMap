package asegroup1.api.models;

import java.util.ArrayList;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

/**
 * Contains the constraints to be passed
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQueryConstraint {

	private String primaryHouseName;
	private String secondaryHouseName;
	private String streetName;
	private String townName;
	private String district;
	private String county;
	private String postCode;
	private PropertyType propertyType;
	private Boolean newBuild;
	private EstateType estateType;
	private TransactionCategory transactionCategory;

	public LandRegistryQueryConstraint(String primaryHouseName, String secondaryHouseName, String streetName, String townName, String district, String county, String postCode,
			PropertyType propertyType,
			Boolean newBuild,
			EstateType estateType, TransactionCategory transactionCategory) {
		this.primaryHouseName = primaryHouseName;
		this.secondaryHouseName = secondaryHouseName;
		this.streetName = streetName;
		this.townName = townName;
		this.district = district;
		this.county = county;
		this.postCode = postCode;

		this.propertyType = propertyType;
		this.newBuild = newBuild;
		this.estateType = estateType;
		this.transactionCategory = transactionCategory;
	}

	public LandRegistryQueryConstraint() {
		this(null, null, null, null, null, null, null, null, null, null, null);
	}

	public String buildQueryWhere() {
		return "WHERE { " + buildQuerySelection() + buildQueryColumns() + "}";
	}

	private String buildQuerySelection() {
		String str = "";
		if (primaryHouseName != null) {
			str += "?addr lrcommon:paon \"" + primaryHouseName + "\". ";
		}
		if (primaryHouseName != null) {
			str += "?addr lrcommon:saon \"" + secondaryHouseName + "\". ";
		}
		if (streetName != null) {
			str += "?addr lrcommon:street \"" + streetName + "\". ";
		}
		if (townName != null) {
			str += "?addr lrcommon:town \"" + townName + "\". ";
		}
		if (district != null) {
			str += "?addr lrcommon:district \"" + district + "\". ";
		}
		if (county != null) {
			str += "?addr lrcommon:county \"" + county + "\". ";
		}
		if (postCode != null) {
			str += "?addr lrcommon:postcode \"" + postCode.toUpperCase() + "\". ";
		}
		
		String nonLocationSelection = "";
		if(propertyType!=null) {
			
		}
		if(newBuild != null) {
			nonLocationSelection+= "lrppi:newBuild "+newBuild+"; ";
		}
		if(estateType != null) {
			nonLocationSelection+= "lrppi:estateType lrcommon:"+estateType.toString().toLowerCase()+"; ";
		}
		if(transactionCategory != null) {
			nonLocationSelection += "lrppi:transactionCategory " + transactionCategory.toString().toLowerCase().replace("_", "") + "; ";
		}
		if(nonLocationSelection.length()>0) {
			str += "?transx " + nonLocationSelection.substring(0, nonLocationSelection.length() - 2) + ". ";
		}
		return str.trim();
	}

	private String buildQueryColumns() {
		return "?transx lrppi:propertyAddress ?addr ; " + "	lrppi:propertyType ?propertyType ; " + "	lrppi:estateType ?estateType ; "
				+ "	lrppi:transactionDate ?transactionDate ; " + "	lrppi:pricePaid ?pricePaid ; " + "	lrppi:newBuild ?newBuild ; "
				+ "	lrppi:transactionCategory ?transactionCategory. " + "OPTIONAL {?addr lrcommon:paon ?paon} " + "OPTIONAL {?addr lrcommon:saon ?saon} "
				+ "OPTIONAL {?addr lrcommon:street ?street} " + "OPTIONAL {?addr lrcommon:locality ?locality} " + "OPTIONAL {?addr lrcommon:town ?town} "
				+ "OPTIONAL {?addr lrcommon:district ?district} " + "OPTIONAL {?addr lrcommon:county ?county} " + "OPTIONAL {?addr lrcommon:postcode ?postcode}";
	}

	public String getPrimaryHouseNameHouseName() {
		return primaryHouseName;
	}

	public void setPrimaryHouseName(String primaryHouseName) {
		this.primaryHouseName = primaryHouseName;
	}

	public String getSecondaryHouseName() {
		return secondaryHouseName;
	}

	public void setSecondaryHouseName(String secondaryHouseName) {
		this.secondaryHouseName = secondaryHouseName;
	}

	public String getStreetName() {
		return streetName;
	}

	public void setStreetName(String streetName) {
		this.streetName = streetName;
	}

	public String getTownName() {
		return townName;
	}

	public void setTownName(String townName) {
		this.townName = townName;
	}

	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	public String getCounty() {
		return county;
	}

	public void setCounty(String county) {
		this.county = county;
	}

	public String getPostCode() {
		return postCode;
	}

	public void setPostCode(String postCode) {
		this.postCode = postCode;
	}

	public PropertyType getPropertyType() {
		return propertyType;
	}

	public void setPropertyType(PropertyType propertyType) {
		this.propertyType = propertyType;
	}

	public Boolean getNewBuild() {
		return newBuild;
	}

	public void setNewBuild(Boolean newBuild) {
		this.newBuild = newBuild;
	}

	public EstateType getEstateType() {
		return estateType;
	}

	public void setEstateType(EstateType estateType) {
		this.estateType = estateType;
	}

	public TransactionCategory getTransactionCategory() {
		return transactionCategory;
	}

	public void setTransactionCategory(TransactionCategory transactionCategory) {
		this.transactionCategory = transactionCategory;
	}

	/**
	 * Validates the parameters provided are valid for the creation of an instance
	 * of {@link LandRegistryQueryConstraint}
	 * 
	 * @return a {@link ResponseEntity} if any of the parameters are malformed, null
	 *         otherwise
	 */
	public static ResponseEntity<?> validateRequestFormat(String primaryHouseName, String secondaryHouseName, String streetName, String townName, String district, String county,
			String postCode,
			PropertyType propertyType, Boolean newBuild, EstateType estateType, TransactionCategory transactionCategory) {
		ArrayList<String> issues = new ArrayList<>();

		addNotNull(issues, isPrimaryHouseNameValid(primaryHouseName));
		addNotNull(issues, isSecondaryHouseNameValid(secondaryHouseName));
		addNotNull(issues, isStreetNameValid(streetName));
		addNotNull(issues, isTownNameValid(townName));
		addNotNull(issues, isDistrictValid(district));
		addNotNull(issues, isCountyValid(postCode));
		addNotNull(issues, isPostCodeValid(postCode));
		addNotNull(issues, isPropertyTypeValid(propertyType));
		addNotNull(issues, isNewBuildValid(newBuild));
		addNotNull(issues, isEstateTypeValid(estateType));
		addNotNull(issues, isTransactionCategoryValid(transactionCategory));


		if (issues.isEmpty()) {

			// check that one of the values is not null (not including price or date)
			if (primaryHouseName != null || streetName != null || townName != null || district != null || county != null || postCode != null || propertyType != null
					|| newBuild != null
					|| estateType != null || transactionCategory != null) {
				return null;
			}
			return new ResponseEntity<>("Invalid Request: Must be provided a selector, that is not a price or date constraint", HttpStatus.BAD_REQUEST);
		} else {
			String message = "Invalid request:";
			for (String issue : issues) {
				message += "\n" + issue;
			}
			return new ResponseEntity<>(message, HttpStatus.BAD_REQUEST);
		}
	}

	private static void addNotNull(ArrayList<String> list, String str) {
		if (str != null) {
			list.add(str);
		}
	}

	public static String isPrimaryHouseNameValid(String primaryHouseName) {
		return null;
	}

	public static String isSecondaryHouseNameValid(String secondaryHouseName) {
		return null;
	}

	public static String isStreetNameValid(String streetName) {
		return null;
	}

	public static String isTownNameValid(String townName) {
		return null;
	}

	public static String isDistrictValid(String district) {
		return null;
	}

	public static String isCountyValid(String county) {
		return null;
	}

	public static String isPostCodeValid(String postCode) {
		if (postCode.charAt(postCode.length() - 4) != 32) {
			return "Post Code: must contain a space in the correct position";
		} else {
			return null;
		}
	}

	public static String isPropertyTypeValid(PropertyType propertyType) {
		return null;
	}

	public static String isNewBuildValid(Boolean newBuild) {
		return null;
	}

	public static String isEstateTypeValid(EstateType estateType2) {
		return null;
	}

	public static String isTransactionCategoryValid(TransactionCategory transactionCategory2) {
		return null;
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
