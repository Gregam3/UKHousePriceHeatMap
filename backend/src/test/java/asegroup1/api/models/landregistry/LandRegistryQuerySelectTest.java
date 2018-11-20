/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQuery.Aggregation;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

/**
 * @author Richousrick
 *
 */
class LandRegistryQuerySelectTest {

	LandRegistryQuerySelect select;




	@BeforeEach
	public void initSelect() {
		select = new LandRegistryQuerySelect();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#LandRegistryQuerySelect(Selectable)}.
	 */
	@Test
	void testLandRegistryQuerySelectEmpty() {
		assertNotNull(select.getSelectValues());
		assertEquals(true, select.getSelectValues().isEmpty());
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#LandRegistryQuerySelect(Selectable)}.
	 */
	@Test
	void testLandRegistryQuerySelectSelectable() {
		List<Selectable> selectables = LandRegistryQueryTestUtils.genRandomSelectables();
		select = new LandRegistryQuerySelect(selectables.toArray(new Selectable[selectables.size()]));
		assertEquals(selectables.size(), select.getSelectValues().size());
		for (Selectable selectable : selectables) {
			assertEquals(Aggregation.SAMPLE, select.getSelectValueAggregation(selectable.toString()));
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#addSelectValue(asegroup1.api.models.landregistry.LandRegistryQuery.Selectable, asegroup1.api.models.landregistry.LandRegistryQuery.Aggregation)}
	 * ,
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#hasValue(java.lang.String)},
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#removeValue(java.lang.String)}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#getSelectValues(java.lang.String)}.
	 */
	@Test
	void testAddSelectValueSelectableAggregation() {
		ArrayList<Selectable> selectableSet = new ArrayList<>(EnumSet.allOf(Selectable.class));
		ArrayList<Aggregation> aggregationSet = new ArrayList<>(EnumSet.allOf(Aggregation.class));
		
		assert selectableSet.size() > aggregationSet.size();

		for (int i = 0; i < aggregationSet.size(); i++) {
			Selectable selectable = selectableSet.get(i);
			Aggregation aggregation = aggregationSet.get(i);

			assertNull(select.getSelectValues(selectable.toString()));
			select.addSelectValue(selectable, aggregation);
			assertTrue(select.hasValue(selectable.toString()));
			assertEquals(aggregation.toString(), select.getSelectValues(selectable.toString())[1]);
			select.removeValue(selectable.toString());
			assertNull(select.getSelectValues(selectable.toString()));
		}
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelect(boolean)}.
	 */
	@Test
	void testBuildQuerySelectIgnoreAggregation() {
		select = LandRegistryQueryTestUtils.genLandRegistryQuerySelect();

		String buildGroup = select.buildQuerySelect(true);

		String regex = LandRegistryQueryTestUtils.buildQuerySelectRegex(true);

		assertTrue(buildGroup.matches(regex));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelect(boolean)}.
	 */
	@Test
	void testBuildQuerySelect() {
		select = LandRegistryQueryTestUtils.genLandRegistryQuerySelect();

		String buildGroup = select.buildQuerySelect(false);

		String regex = LandRegistryQueryTestUtils.buildQuerySelectRegex(false);

		assertTrue(buildGroup.matches(regex));
	}

}
