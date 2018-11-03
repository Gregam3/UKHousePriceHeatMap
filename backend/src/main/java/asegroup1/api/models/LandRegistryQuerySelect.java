package asegroup1.api.models;

/**
 * 
 * @author Rikkey Paal
 */
public class LandRegistryQuerySelect {

	private boolean propertyType;
	private boolean estateType;
	private boolean transactionDate;
	private boolean pricePaid;
	private boolean newBuild;
	private boolean transactionCategory;
	private boolean paon;
	private boolean saon;
	private boolean street;
	private boolean locality;
	private boolean town;
	private boolean district;
	private boolean county;
	private boolean postcode;
	private boolean date;
	private boolean housePriceIndex;

	public LandRegistryQuerySelect(boolean propertyType, boolean estateType, boolean transactionDate, boolean pricePaid, boolean newBuild, boolean transactionCategory,
			boolean paon, boolean saon, boolean street, boolean locality, boolean town, boolean district, boolean county, boolean postcode, boolean date, boolean housePriceIndex) {
		this.propertyType = propertyType;
		this.estateType = estateType;
		this.transactionDate = transactionDate;
		this.pricePaid = pricePaid;
		this.newBuild = newBuild;
		this.transactionCategory = transactionCategory;
		this.paon = paon;
		this.saon = saon;
		this.street = street;
		this.locality = locality;
		this.town = town;
		this.district = district;
		this.county = county;
		this.postcode = postcode;
		this.date = date;
		this.housePriceIndex = housePriceIndex;
	}

	public LandRegistryQuerySelect() {
		this(false, false, false, false, false, false, false, false, false, false, false, false, false, false, false, false);
	}

	public String buildQuerySelect() {
		String str = "SELECT ";
		if (isPropertyType()) {
			str += "?propertyType ";
		}
		if (isEstateType()) {
			str += "?estateType ";
		}
		if (isTransactionDate()) {
			str += "?transactionDate ";
		}
		if (isPricePaid()) {
			str += "?pricePaid ";
		}
		if (isNewBuild()) {
			str += "?newBuild ";
		}
		if (isTransactionCategory()) {
			str += "?transactionCategory ";
		}
		if (isPaon()) {
			str += "?paon ";
		}
		if (isSaon()) {
			str += "?saon ";
		}
		if (isStreet()) {
			str += "?street ";
		}
		if (isLocality()) {
			str += "?locality ";
		}
		if (isTown()) {
			str += "?town ";
		}
		if (isDistrict()) {
			str += "?district ";
		}
		if (isCounty()) {
			str += "?county ";
		}
		if (isPostcode()) {
			str += "?postcode ";
		}
		if (isDate()) {
			str += "?date ";
		}
		if (isHousePriceIndex()) {
			str += "?hpi ";
		}
		return str.trim();
	}
	
	public boolean isPropertyType() {
		return propertyType;
	}

	public void setPropertyType(boolean propertyType) {
		this.propertyType = propertyType;
	}

	public boolean isEstateType() {
		return estateType;
	}

	public void setEstateType(boolean estateType) {
		this.estateType = estateType;
	}

	public boolean isTransactionDate() {
		return transactionDate;
	}

	public void setTransactionDate(boolean transactionDate) {
		this.transactionDate = transactionDate;
	}

	public boolean isPricePaid() {
		return pricePaid;
	}

	public void setPricePaid(boolean pricePaid) {
		this.pricePaid = pricePaid;
	}

	public boolean isNewBuild() {
		return newBuild;
	}

	public void setNewBuild(boolean newBuild) {
		this.newBuild = newBuild;
	}

	public boolean isTransactionCategory() {
		return transactionCategory;
	}

	public void setTransactionCategory(boolean transactionCategory) {
		this.transactionCategory = transactionCategory;
	}

	public boolean isPaon() {
		return paon;
	}

	public void setPaon(boolean paon) {
		this.paon = paon;
	}

	public boolean isSaon() {
		return saon;
	}

	public void setSaon(boolean saon) {
		this.saon = saon;
	}

	public boolean isStreet() {
		return street;
	}

	public void setStreet(boolean street) {
		this.street = street;
	}

	public boolean isLocality() {
		return locality;
	}

	public void setLocality(boolean locality) {
		this.locality = locality;
	}

	public boolean isTown() {
		return town;
	}

	public void setTown(boolean town) {
		this.town = town;
	}

	public boolean isDistrict() {
		return district;
	}

	public void setDistrict(boolean district) {
		this.district = district;
	}

	public boolean isCounty() {
		return county;
	}

	public void setCounty(boolean county) {
		this.county = county;
	}

	public boolean isPostcode() {
		return postcode;
	}

	public void setPostcode(boolean postcode) {
		this.postcode = postcode;
	}

	public boolean isDate() {
		return date;
	}

	public void setDate(boolean date) {
		this.date = date;
	}

	public boolean isHousePriceIndex() {
		return housePriceIndex;
	}

	public void setHousePriceIndex(boolean housePriceIndex) {
		this.housePriceIndex = housePriceIndex;
	}

}
