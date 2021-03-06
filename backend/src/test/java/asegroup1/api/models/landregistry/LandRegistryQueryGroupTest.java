/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;


/**
 * @author Richousrick
 *
 */
class LandRegistryQueryGroupTest {

	private EnumSet<Selectable> selectables;
	private LandRegistryQueryGroup group;

	private List<Selectable> fillWithRandomData() {
		List<Selectable> select = LandRegistryQueryTestUtils.genRandomSelectables();
		group.select(LandRegistryQueryTestUtils.buildRandomSelectablesArray(select));
		return select;
	}

	@BeforeEach
	public void initTests() {
		group = new LandRegistryQueryGroup();
		selectables = EnumSet.allOf(Selectable.class);
	}


	private void testQuerySelectEmpty() {
		selectables.forEach(v -> {
			assertFalse(group.hasSelectable(v.toString()));
		});
	}

	private void testQuerySelectMatches(List<Selectable> selected) {
		ArrayList<Selectable> nonExistant = new ArrayList<>(selectables);
		nonExistant.removeAll(selected);
		for (Selectable select : selected) {
			assertTrue(group.hasSelectable(select.toString()));
		}

		for (Selectable select : nonExistant) {
			assertFalse(group.hasSelectable(select.toString()));
		}
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#LandRegistryQueryGroup(String...)}.
	 * with no parameters provided.
	 */
	@Test
	public void testLandRegistryQuerySelect() {
		testQuerySelectEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#LandRegistryQueryGroup(String...)}.
	 * with duplicate parameters provided.
	 */
	@Test
	public void testLandRegistryQuerySelectDuplicate() {
		group = new LandRegistryQueryGroup("test", "test", "other");
		assertTrue(group.hasSelectable("test"));
		assertTrue(group.hasSelectable("other"));
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#LandRegistryQueryGroup(String...)}.
	 */
	@Test
	public void testLandRegistryQuerySelectSelectableArray() {
		List<Selectable> select = LandRegistryQueryTestUtils.genRandomSelectables();
		group = new LandRegistryQueryGroup(LandRegistryQueryTestUtils.buildRandomSelectablesArray(select));
		testQuerySelectMatches(select);
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#select(String...)}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#deselect(String...)}
	 */
	@Test
	public void testSelectDeselectOne() {
		List<Selectable> select = LandRegistryQueryTestUtils.genRandomSelectables();
		for (Selectable selectable : select) {
			group.select(selectable.toString());
			assertTrue(group.hasSelectable(selectable.toString()));
			group.deselect(selectable.toString());
			assertFalse(group.hasSelectable(selectable.toString()));
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#select(String...)}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#deselect(String...)}.
	 */
	@Test
	public void testSelectDeselectMany() {
		List<Selectable> select = fillWithRandomData();
		testQuerySelectMatches(select);
		group.deselect(LandRegistryQueryTestUtils.buildRandomSelectablesArray(select));
		testQuerySelectEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#select(String...)}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#deselectAll()}.
	 */
	@Test
	public void testDeselectAll() {
		List<Selectable> select = fillWithRandomData();
		testQuerySelectMatches(select);
		group.deselectAll();
		testQuerySelectEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#toggleSelectable(String)}.
	 */
	@Test
	public void testToggleSelectable() {
		List<Selectable> initial = fillWithRandomData();
		long tmp = LandRegistryQueryTestUtils.randomSeed;
		LandRegistryQueryTestUtils.randomSeed = 12;
		List<Selectable> toToggle = LandRegistryQueryTestUtils.genRandomSelectables();
		LandRegistryQueryTestUtils.randomSeed = tmp;
		List<Selectable> expected = new ArrayList<Selectable>(initial);
		toToggle.forEach(v -> {
			if (expected.contains(v)) {
				expected.remove(v);
			} else {
				expected.add(v);
			}
		});

		for (Selectable selectable : toToggle) {
			group.toggleSelectable(selectable.toString());
		}

		testQuerySelectMatches(expected);

	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#buildQuerySelect()}.
	 */
	@Test
	public void testBuildQuerySelect() {
		List<Selectable> initial = fillWithRandomData();

		String buildGroup = group.buildGroup();

		String regex = LandRegistryQueryTestUtils.buildQueryGroupRegex(initial);

		assertTrue(buildGroup.matches(regex));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#getSelectables()}.
	 */
	@Test
	public void testBuildQuerySelecta() {
		List<String> initial = fillWithRandomData().stream().map(String::valueOf).collect(Collectors.toList());
		assertEquals(initial, group.getSelectables());
	}

}
