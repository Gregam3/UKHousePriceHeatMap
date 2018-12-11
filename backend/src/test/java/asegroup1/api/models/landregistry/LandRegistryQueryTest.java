/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

/**
 * @author Richousrick
 *
 */
class LandRegistryQueryTest {

	LandRegistryQuery query;



	@BeforeEach
	public void initSelect() {
		query = new LandRegistryQuery();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuery#LandRegistryQuery()}.
	 */
	@Test
	void testLandRegistryQuery() {
		assertNotNull(query.getBody());
		assertNotNull(query.getGroupConstraint());
		assertNotNull(query.getSelect());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuery#buildQueryContent()}.
	 */
	@Test
	void testBuildQueryContentConstraintContent() {
		query = LandRegistryQueryTestUtils.genLandRegistryQuery(true);

		String buildGroup = query.buildQueryContent();

		String regex = LandRegistryQueryTestUtils.buildQueryRegexInternal(0);

		assertTrue(buildGroup.matches(regex));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuery#buildQuery()}.
	 */
	@Test
	void testBuildQueryRecusrsiveContent() {
		query = LandRegistryQueryTestUtils.genLandRegistryQuery(false);

		String buildGroup = query.buildQueryContent();

		String regex = LandRegistryQueryTestUtils.buildQueryRegexInternal(1);

		assertTrue(buildGroup.matches(regex));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuery#buildQueryAggregatePostCode(LandRegistryQueryBody, String, String, String)}.
	 */
	@Test
	void testBuildQueryAggregatePostCode() {
		LandRegistryQueryConstraint body = LandRegistryQueryTestUtils.genLandRegistryQueryConstraint();
		body.setEqualityConstraint(Selectable.postcode, LandRegistryQueryTestUtils.getPostCodes());
		String postCodeName = "postcode";
		String pricePaidName = "PricePaid";
		String pricePaidFieldName = "pricePaid";
		LandRegistryQuery query = LandRegistryQuery.buildQueryAggregatePostCode(body, postCodeName, pricePaidName, pricePaidFieldName);
		assertTrue(query.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegexInternal(0)));
		
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuery#buildQueryAveragePricePostcode(String...)}.
	 */
	@Test
	void testBuildQueryAveragePostCodeList() {
		LandRegistryQuery query = LandRegistryQuery.buildQueryAveragePricePostcode(LandRegistryQueryTestUtils.getPostCodes());
		assertTrue(query.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegexInternal(1)));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuery#setBody(LandRegistryQueryBody)}.
	 */
	@Test
	void testSetBody() {
		assertNotNull(query.getBody());
		query.setBody(null);
		assertNull(query.getBody());
		LandRegistryQueryConstraint cons = LandRegistryQueryTestUtils.genLandRegistryQueryConstraint();
		query.setBody(cons);
		assertNotNull(query.getBody());
		assertEquals(cons, query.getBody());
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuery#setGroupConstraint(LandRegistryQueryGroup)}.
	 */
	@Test
	void testSetGroupConstraint() {
		assertNotNull(query.getGroupConstraint());
		query.setGroupConstraint(null);
		assertNull(query.getGroupConstraint());
		LandRegistryQueryGroup group = LandRegistryQueryTestUtils.genLandRegistryQueryGroup();
		query.setGroupConstraint(group);
		assertNotNull(query.getGroupConstraint());
		assertEquals(group, query.getGroupConstraint());
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuery#setSelect(LandRegistryQueryGroup)}.
	 */
	@Test
	void testSetSelect() {
		assertNotNull(query.getSelect());
		query.setSelect(null);
		assertNull(query.getSelect());
		LandRegistryQuerySelect select = LandRegistryQueryTestUtils.genLandRegistryQuerySelect();
		query.setSelect(select);
		assertNotNull(query.getSelect());
		assertEquals(select, query.getSelect());
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuery#buildQueryAveragePricePostcode(String...)}.
	 */
	@Test
	void testBuildQueryAveragePostCodeRegex() {
		LandRegistryQuery query = LandRegistryQuery.buildQueryAveragePricePostcode("BN23 7", "Eastbourne");
		assertTrue(query.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegexInternal(1)));
	}
	
	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuery#}
	 */
	@Test
	void testBuildQuery() {
		LandRegistryQuery query = LandRegistryQuery.buildQueryAveragePricePostcode(LandRegistryQueryTestUtils.getPostCodes());
		assertTrue(query.buildQuery().matches(LandRegistryQueryTestUtils.buildQueryRegex(1)));
	}
}
