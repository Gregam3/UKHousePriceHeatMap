/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.Assert.assertFalse;
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
import asegroup1.api.models.landregistry.LandRegistryQuerySelect.SelectObj;

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
			assertEquals(Aggregation.SAMPLE, select.getSelectObj(selectable.toString()).aggregation);
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
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#removeValue(String)}.
	 */
	@Test
	void testRemoveValue() {
		String ref = "test";
		select.addSelectValue(ref);
		assertTrue(select.hasValue(ref));
		assertTrue(select.removeValue(ref));
		assertFalse(select.hasValue(ref));
		assertFalse(select.removeValue(ref));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelect(boolean)}.
	 */
	@Test
	void testBuildQuerySelect() {
		select = LandRegistryQueryTestUtils.genLandRegistryQuerySelect();
		select.addSelectValue("avg", Aggregation.AVG, "AVG");
		select.addSelectValue("count", Aggregation.COUNT, "COUNT");
		select.addSelectValue("max", Aggregation.MAX, "MAX");
		select.addSelectValue("min", Aggregation.MIN, "MIN");
		select.addSelectValue("none", Aggregation.NONE, "NONE");
		select.addSelectValue("sample", Aggregation.SAMPLE, "SAMPLE");
		select.addSelectValue("sum", Aggregation.SUM, "SUM");

		String buildGroup = select.buildQuerySelect(false);

		String regex = LandRegistryQueryTestUtils.buildQuerySelectRegex(false);
		System.out.println(buildGroup);
		assertTrue(buildGroup.matches(regex));
		for (SelectObj obj : select.getSelectValues().values()) {
			assertTrue(("SELECT " + obj).toString().matches(regex));
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#addSelectValue(String)}.
	 */
	@Test
	void testAddSelectValue() {
		select.addSelectValue("test");
		SelectObj obj = select.getSelectObj("test");
		assertEquals(select.new SelectObj("test", null, Aggregation.NONE), obj);
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect.SelectObj#equals(Object)}.
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	void testSelectObjEquals() {
		SelectObj o1 = select.new SelectObj("avg", "AVG", Aggregation.AVG);
		SelectObj o6 = select.new SelectObj("avg", "AVG", Aggregation.AVG);

		SelectObj o2 = select.new SelectObj("count", "COUNT", Aggregation.COUNT);
		SelectObj o3 = select.new SelectObj("null", null, Aggregation.AVG);
		SelectObj o4 = select.new SelectObj(null, "AVG", Aggregation.MIN);
		SelectObj o5 = select.new SelectObj("avg", "NONE", Aggregation.NONE);

		SelectObj o7 = select.new SelectObj("avg", "AVG", Aggregation.SUM);
		SelectObj o8 = select.new SelectObj("null", "AVG", Aggregation.AVG);
		SelectObj o9 = select.new SelectObj("avg", "null", Aggregation.AVG);

		SelectObj o10 = select.new SelectObj(null, "null", Aggregation.AVG);
		SelectObj o11 = select.new SelectObj(null, "null", Aggregation.AVG);
		SelectObj o12 = select.new SelectObj("null", "null", Aggregation.AVG);

		assertTrue(o1.equals(o1));
		assertTrue(o1.equals(o6));
		assertTrue(o10.equals(o11));
		
		assertFalse(o10.equals(o12));

		assertFalse(o1.equals(o2));
		assertFalse(o1.equals(o3));
		assertFalse(o1.equals(o4));
		assertFalse(o1.equals(o5));

		assertFalse(o1.equals(o7));
		assertFalse(o1.equals(o8));
		assertFalse(o1.equals(o9));

		assertFalse(o1.equals("wrong"));

	}
}
