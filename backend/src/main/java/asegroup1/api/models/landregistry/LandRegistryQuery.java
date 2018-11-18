package asegroup1.api.models.landregistry;

import java.util.List;

public class LandRegistryQuery implements LandRegistryQueryBody {

	private LandRegistryQueryBody body;

	private LandRegistryQueryGroup groupConstraint;

	private LandRegistryQuerySelect select;


	public LandRegistryQuery(LandRegistryQueryBody body, LandRegistryQueryGroup groupConstraint, LandRegistryQuerySelect select) {
		this.body = body;
		this.groupConstraint = groupConstraint;
		this.select = select;
	}

	public LandRegistryQuery() {
		body = new LandRegistryQueryConstraint();
		groupConstraint = new LandRegistryQueryGroup();
		select = new LandRegistryQuerySelect();
	}

	public LandRegistryQueryBody getBody() {
		return body;
	}

	public void setBody(LandRegistryQueryBody body) {
		this.body = body;
	}

	public LandRegistryQueryGroup getGroupConstraint() {
		return groupConstraint;
	}

	public void setGroupConstraint(LandRegistryQueryGroup groupConstraint) {
		this.groupConstraint = groupConstraint;
	}

	public LandRegistryQuerySelect getSelect() {
		return select;
	}

	public void setSelect(LandRegistryQuerySelect select) {
		this.select = select;
	}

	public void addVarToGroup(String varname, boolean addToSelect) {
		getGroupConstraint().select(varname);
		if (addToSelect) {
			getSelect().addSelectValue(varname);
		}
	}

	public void addVarToGroup(String varname) {
		addVarToGroup(varname, false);
	}

	public void addVarToSelect(String referenceName, Aggrigation aggrigation, String aggrigationResult) {
		getSelect().addSelectValue(referenceName, aggrigation, aggrigationResult);
	}

	@Override
	public String buildQueryContent() {
		boolean grouping = !(groupConstraint == null || groupConstraint.getSelectables().isEmpty());
		StringBuilder queryBuilder = new StringBuilder(select.buildQuerySelect(!grouping));
		queryBuilder.append("\n");
		queryBuilder.append("WHERE { \n\t" + body.buildQueryContent().replace("\n", "\n\t") + "\n}");
		if (grouping) {
			queryBuilder.append("\n");
			queryBuilder.append(groupConstraint.buildGroup());
		}
		return queryBuilder.toString().trim();
	}


	public String buildQuery() {
		return getQueryPrefixDeclarations() + "\n" + buildQueryContent();
	}

	private String getQueryPrefixDeclarations() {
		return "prefix rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#> \n" + "prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> \n"
				+ "prefix owl: <http://www.w3.org/2002/07/owl#> \n" + "prefix xsd: <http://www.w3.org/2001/XMLSchema#> \n"
				+ "prefix sr: <http://data.ordnancesurvey.co.uk/ontology/spatialrelations/> \n" + "prefix ukhpi: <http://landregistry.data.gov.uk/def/ukhpi/> \n"
				+ "prefix lrppi: <http://landregistry.data.gov.uk/def/ppi/> \n" + "prefix skos: <http://www.w3.org/2004/02/skos/core#> \n"
				+ "prefix lrcommon: <http://landregistry.data.gov.uk/def/common/>";
	}

	public static LandRegistryQuery buildQueryLatestSalesOnly(LandRegistryQueryConstraint body, List<Selectable> selectables) {

		LandRegistryQueryGroup group = new LandRegistryQueryGroup("paon", "saon", "street", "postcode");

		LandRegistryQuerySelect select = new LandRegistryQuerySelect();
		select.addSelectValue(Selectable.paon, Aggrigation.NONE);
		selectables.remove(Selectable.paon);
		select.addSelectValue(Selectable.saon, Aggrigation.NONE);
		selectables.remove(Selectable.saon);
		select.addSelectValue(Selectable.street, Aggrigation.NONE);
		selectables.remove(Selectable.street);
		select.addSelectValue(Selectable.postcode, Aggrigation.NONE);
		selectables.remove(Selectable.postcode);
		select.addSelectValue(Selectable.transactionDate, Aggrigation.MAX);
		selectables.remove(Selectable.transactionDate);

		for (Selectable selectable : selectables) {
			select.addSelectValue(selectable, Aggrigation.SAMPLE);
		}
		
		return new LandRegistryQuery(body, group, select);
	}

	public static LandRegistryQuery buildQueryAggrigatePostCode(LandRegistryQueryBody body, String postCodeName, String pricePaidName, String pricePaidFieldName) {

		LandRegistryQueryGroup groupConstraint = new LandRegistryQueryGroup(postCodeName);
		LandRegistryQuerySelect select = new LandRegistryQuerySelect();
		select.addSelectValue(postCodeName, Aggrigation.NONE, "");
		select.addSelectValue(pricePaidName, Aggrigation.AVG, pricePaidFieldName);

		return new LandRegistryQuery(body, groupConstraint, select);
	}



	public enum Selectable {
		propertyType, estateType, transactionDate, pricePaid, newBuild, transactionCategory, paon, saon, street, locality, town, district, county, postcode;
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

	public enum Aggrigation {
		COUNT, SUM, AVG, MIN, MAX, SAMPLE, NONE;
	}
	
	

}
