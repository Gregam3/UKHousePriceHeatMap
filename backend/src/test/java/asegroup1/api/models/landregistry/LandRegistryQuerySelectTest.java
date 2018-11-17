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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQuery.Aggrigation;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;

/**
 * @author Richousrick
 *
 */
class LandRegistryQuerySelectTest {

	private LandRegistryQuerySelect select;

	private LinkedHashMap<Selectable, Aggrigation> fillWithRandomData() {
		ArrayList<Selectable> selectableSet = new ArrayList<>(EnumSet.allOf(Selectable.class));
		ArrayList<Aggrigation> aggrigationSet = new ArrayList<>(EnumSet.allOf(Aggrigation.class));
		Random rand = new Random(LandRegistryQueryTestUtils.randomSeed);

		LinkedHashMap<Selectable, Aggrigation> map = new LinkedHashMap<>();

		int iterations = rand.nextInt(selectableSet.size() - 4) + 3;
		for (int i = 0; i < iterations; i++) {
			Selectable selectable = selectableSet.remove(rand.nextInt(selectableSet.size()));
			Aggrigation aggrigation = aggrigationSet.get(rand.nextInt(aggrigationSet.size()));
			select.setSelectValue(selectable, aggrigation);
			map.put(selectable, aggrigation);
		}
		return map;
	}

	@BeforeEach
	public void initSelect() {
		select = new LandRegistryQuerySelect();
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#LandRegistryQuerySelect()}.
	 */
	@Test
	void testLandRegistryQuerySelect() {
		assertNotNull(select.getSelectValues());
		assertEquals(true, select.getSelectValues().isEmpty());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#LandRegistryQuerySelect(asegroup1.api.models.landregistry.LandRegistryQuery.Selectable[])}.
	 */
	@Test
	void testLandRegistryQuerySelectSelectableArray() {
		List<Selectable> selectables = LandRegistryQueryTestUtils.genRandomSelectableSelection();
		select = new LandRegistryQuerySelect(selectables.toArray(new Selectable[selectables.size()]));
		assertEquals(selectables.size(), select.getSelectValues().size());
		for (Selectable selectable : selectables) {
			assertEquals(Aggrigation.SAMPLE, select.getSelectValues(selectable));
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#setSelectValue(asegroup1.api.models.landregistry.LandRegistryQuery.Selectable, asegroup1.api.models.landregistry.LandRegistryQuery.Aggrigation)}
	 * ,
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#hasValue(asegroup1.api.models.landregistry.LandRegistryQuery.Selectable)},
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#getSelectValues(asegroup1.api.models.landregistry.LandRegistryQuery.Selectable)}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#getSelectValues(asegroup1.api.models.landregistry.LandRegistryQuery.Selectable)}.
	 */
	@Test
	void testSetHasRemoveSelectValue() {
		ArrayList<Selectable> selectableSet = new ArrayList<>(EnumSet.allOf(Selectable.class));
		ArrayList<Aggrigation> aggrigationSet = new ArrayList<>(EnumSet.allOf(Aggrigation.class));
		
		assert selectableSet.size() > aggrigationSet.size();

		for (int i = 0; i < aggrigationSet.size(); i++) {
			Selectable selectable = selectableSet.get(i);
			Aggrigation aggrigation = aggrigationSet.get(i);

			assertNull(select.getSelectValues(selectable));
			select.setSelectValue(selectable, aggrigation);
			assertTrue(select.hasValue(selectable));
			assertEquals(aggrigation, select.getSelectValues(selectable));
			select.removeValue(selectable);
			assertNull(select.getSelectValues(selectable));
		}
		
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelect(boolean)}.
	 */
	@Test
	void testBuildQuerySelectIgnore() {
		fillWithRandomData();

		String build = select.buildQuerySelect(false);

		String regex = LandRegistryQueryTestUtils.buildQuerySelectRegex(false);

		assertTrue(build.matches(regex));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQuerySelect#buildQuerySelect(boolean)}.
	 */
	@Test
	void testBuildQuerySelect() {
		fillWithRandomData();

		String build = select.buildQuerySelect(true);

		String regex = LandRegistryQueryTestUtils.buildQuerySelectRegex(true);

		assertTrue(build.matches(regex));
	}



}
