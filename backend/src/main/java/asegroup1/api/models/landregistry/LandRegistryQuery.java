package asegroup1.api.models.landregistry;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Rikkey Paal
 */

public class LandRegistryQuery implements LandRegistryQueryBody {

	private LandRegistryQueryBody body;

	private LandRegistryQueryGroup groupConstraint;

	private LandRegistryQuerySelect select;


	public LandRegistryQuery(LandRegistryQueryBody body, LandRegistryQueryGroup groupConstraint, LandRegistryQuerySelect select) {
		this.body = body;
		this.groupConstraint = groupConstraint;
		this.select = select;
	}

	public LandRegistryQuery(LandRegistryQueryBody body, Selectable... selectables) {
		this(body, null, new LandRegistryQuerySelect(selectables));
	}

	public LandRegistryQuery() {
		this(new LandRegistryQueryConstraint(), new LandRegistryQueryGroup(), new LandRegistryQuerySelect());
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

	public void addVarToSelect(String referenceName, Aggregation aggregation, String aggregationResult) {
		getSelect().addSelectValue(referenceName, aggregation, aggregationResult);
	}

	@Override
	public String buildQueryContent() {
		boolean grouping = !(groupConstraint == null || groupConstraint.getSelectables().isEmpty());
		StringBuilder queryBuilder = new StringBuilder(select.buildQuerySelect(!grouping)).append("\n").append("WHERE { \n\t")
				.append(body.buildQueryContent().replace("\n", "\n\t")).append("\n}");
		if (grouping) {
			queryBuilder.append("\n").append(groupConstraint.buildGroup());
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
		selectables = new ArrayList<>(selectables);
		LandRegistryQueryGroup group = new LandRegistryQueryGroup("paon", "saon", "street", "postcode");

		LandRegistryQuerySelect select = new LandRegistryQuerySelect();
		select.addSelectValue(Selectable.paon, Aggregation.NONE);
		selectables.remove(Selectable.paon);
		select.addSelectValue(Selectable.saon, Aggregation.NONE);
		selectables.remove(Selectable.saon);
		select.addSelectValue(Selectable.street, Aggregation.NONE);
		selectables.remove(Selectable.street);
		select.addSelectValue(Selectable.postcode, Aggregation.NONE);
		selectables.remove(Selectable.postcode);
		select.addSelectValue(Selectable.transactionDate, Aggregation.MAX);
		selectables.remove(Selectable.transactionDate);

		for (Selectable selectable : selectables) {
			select.addSelectValue(selectable, Aggregation.SAMPLE);
		}
		
		return new LandRegistryQuery(body, group, select);
	}

	public static LandRegistryQuery buildQueryAggregatePostCode(LandRegistryQueryBody body, String postCodeName, String pricePaidName, String pricePaidFieldName) {

		LandRegistryQueryGroup groupConstraint = new LandRegistryQueryGroup(postCodeName);
		LandRegistryQuerySelect select = new LandRegistryQuerySelect();
		select.addSelectValue(postCodeName, Aggregation.NONE, "");
		select.addSelectValue(pricePaidName, Aggregation.AVG, pricePaidFieldName);

		return new LandRegistryQuery(body, groupConstraint, select);
	}

	public static LandRegistryQuery buildQueryAveragePricePostcode() {
		LandRegistryQueryConstraint constraint = new LandRegistryQueryConstraint();
		constraint.setEqualityConstraint(Selectable.town, "eastbourne");
		constraint.setPostcodeRegex("BN23 7L");
		return buildQueryAggregatePostCode(LandRegistryQuery.buildQueryLatestSalesOnly(constraint, Arrays.asList(Selectable.pricePaid)), "postcode", "PricePaid", "pricePaid");
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

	public enum Aggregation {
		COUNT, SUM, AVG, MIN, MAX, SAMPLE, NONE;
	}
	
}
