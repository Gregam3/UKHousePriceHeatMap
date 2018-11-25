/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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

}
