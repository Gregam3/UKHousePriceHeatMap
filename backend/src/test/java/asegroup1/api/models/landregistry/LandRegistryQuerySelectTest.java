/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable;

/**
 * @author Richousrick
 *
 */
class LandRegistryQuerySelectTest {

	private EnumSet<Selectable> selectables;
	private LandRegistryQueryGroup querySelect;

	private static long randomSeed = 8312595207343625996L;


	@BeforeEach
	public void initTests() {
		querySelect = new LandRegistryQueryGroup();
		selectables = EnumSet.allOf(Selectable.class);
	}


	private void testQuerySelectEmpty() {
		selectables.forEach(v -> {
			assertFalse(querySelect.hasSelectable(v));
		});
	}

	private void testQuerySelectFull() {
		selectables.forEach(v -> {
			assertTrue(querySelect.hasSelectable(v));
		});
	}

	private void testQuerySelectMatches(List<Selectable> selected) {
		ArrayList<Selectable> nonExistant = new ArrayList<>(selectables);
		nonExistant.removeAll(selected);
		for (Selectable select : selected) {
			assertTrue(querySelect.hasSelectable(select));
		}

		for (Selectable select : nonExistant) {
			assertFalse(querySelect.hasSelectable(select));
		}
	}

	private List<Selectable> genRandomSelectables() {
		Random r = new Random(randomSeed);
		ArrayList<Selectable> unSelected = new ArrayList<>(selectables);
		ArrayList<Selectable> selected = new ArrayList<>();
		int reps = r.nextInt(selectables.size() - 1) + 1;
		for (int i = 0; i < reps; i++) {
			selected.add(unSelected.remove(r.nextInt(unSelected.size())));
		}

		return selected;
	}

	private Selectable[] buildRandomSelectablesArray(List<Selectable> select) {
		return select.toArray(new Selectable[select.size()]);
	}

	private List<Selectable> fillWithRandomData() {
		List<Selectable> select = genRandomSelectables();
		Selectable[] selectArr = buildRandomSelectablesArray(select);
		querySelect.select(selectArr);
		return select;
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#LandRegistryQuerySelect(asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable[])}.
	 * with no parameters provided.
	 */
	@Test
	public void testLandRegistryQuerySelect() {
		testQuerySelectEmpty();
	}


	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#LandRegistryQuerySelect(asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable[])}.
	 */
	@Test
	public void testLandRegistryQuerySelectSelectableArray() {
		List<Selectable> select = genRandomSelectables();
		querySelect = new LandRegistryQueryGroup(buildRandomSelectablesArray(select));
		testQuerySelectMatches(select);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#LandRegistryQuerySelect(boolean, asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable[])}.
	 */
	@Test
	public void testLandRegistryQuerySelectBooleanSelectableArray() {
		List<Selectable> select = genRandomSelectables();
		querySelect = new LandRegistryQueryGroup(false, buildRandomSelectablesArray(select));
		testQuerySelectMatches(select);


		select = genRandomSelectables();
		ArrayList<Selectable> inverse = new ArrayList<>(selectables);
		inverse.removeAll(select);
		querySelect = new LandRegistryQueryGroup(true, buildRandomSelectablesArray(select));
		testQuerySelectMatches(inverse);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#select(asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable[])}.
	 */
	@Test
	public void testSelectDeselectOne() {
		List<Selectable> select = genRandomSelectables();
		for (Selectable selectable : select) {
			querySelect.select(selectable);
			assertTrue(querySelect.hasSelectable(selectable));
			querySelect.deselect(selectable);
			assertFalse(querySelect.hasSelectable(selectable));
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#select(asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable[])}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#deselect(asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable[])}.
	 */
	@Test
	public void testSelectDeselectMany() {
		List<Selectable> select = fillWithRandomData();
		testQuerySelectMatches(select);
		querySelect.deselect(buildRandomSelectablesArray(select));
		testQuerySelectEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#selectAll()}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#deselectAll()}.
	 */
	@Test
	public void testSelectDeselectAllEmpty() {
		querySelect.selectAll();
		testQuerySelectFull();
		querySelect.deselectAll();
		testQuerySelectEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#selectAll()}
	 * when there is some data in select
	 */
	@Test
	public void testSelectAllMixed() {
		fillWithRandomData();
		querySelect.selectAll();
		testQuerySelectFull();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#deselectAll()}
	 * when there is some data in select
	 */
	@Test
	public void testDeselectAllMixed() {
		fillWithRandomData();
		querySelect.deselectAll();
		testQuerySelectEmpty();
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#toggleSelectable(asegroup1.api.models.landregistry.LandRegistryQueryGroup.Selectable)}.
	 */
	@Test
	public void testToggleSelectable() {
		List<Selectable> initial = fillWithRandomData();
		List<Selectable> toToggle = genRandomSelectables();
		List<Selectable> expected = new ArrayList<Selectable>(initial);
		toToggle.forEach(v -> {
			if (expected.contains(v)) {
				expected.remove(v);
			} else {
				expected.add(v);
			}
		});

		for (Selectable selectable : toToggle) {
			querySelect.toggleSelectable(selectable);
		}

		testQuerySelectMatches(expected);

	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#buildQuerySelect()}.
	 */
	@Test
	public void testBuildQuerySelect() {
		List<Selectable> initial = fillWithRandomData();
		String regex;
		StringBuilder regexBuilder = new StringBuilder("SELECT(\\s\\?(");
		initial.forEach(v -> {
			regexBuilder.append("(" + v.toString() + ")|");
		});
		regexBuilder.deleteCharAt(regexBuilder.length() - 1);
		regexBuilder.append("))+");
		regex = regexBuilder.toString();

		assertTrue(querySelect.buildQuerySelect().matches(regex));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#buildQuerySelectUnique()}.
	 */
	@Test
	public void testBuildQuerySelectUniqueFilled() {
		ArrayList<Selectable> usedSelectables = new ArrayList<>(
				Arrays.asList(new Selectable[] { Selectable.paon, Selectable.saon, Selectable.street, Selectable.postcode, Selectable.transactionDate }));
		selectables.removeAll(usedSelectables);
		List<Selectable> initial = fillWithRandomData();
		testBuildQuerySelectUnique(initial);
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryGroup#buildQuerySelectUnique()}.
	 */
	@Test
	public void testBuildQuerySelectUniqueEmpty() {
		testBuildQuerySelectUnique(new ArrayList<>());
	}


	private void testBuildQuerySelectUnique(List<Selectable> initial) {
		String regex = buildQuerySelectRegex(initial);

		System.out.println(querySelect.buildQuerySelectUnique());

		assertTrue(querySelect.buildQuerySelectUnique().matches(regex));
	}



	private String buildQuerySelectRegex(List<Selectable> selectables) {
		StringBuilder select = new StringBuilder("SELECT \\?paon \\?saon \\?street \\?postcode \\(max\\(\\?transactionDate\\) AS \\?TransactionDate\\)");



		if (!selectables.isEmpty()) {
			StringBuilder extention = new StringBuilder();
			StringBuilder sample = new StringBuilder("SAMPLE\\(\\?");
			StringBuilder variableLower = new StringBuilder();
			StringBuilder variableUpper = new StringBuilder();
			selectables.forEach(v -> {
				variableLower.append("(" + v.toString() + ")|");
				variableUpper.append("(" + v.toString().substring(0, 1).toUpperCase() + v.toString().substring(1) + ")|");
			});
			variableLower.deleteCharAt(variableLower.length() - 1);
			variableUpper.deleteCharAt(variableUpper.length() - 1);

			sample.append("(" + variableLower + ")\\) AS \\?(" + variableUpper + ")");
			System.out.println(sample);

			extention.append("(\\(" + sample + "\\)\\s*)+");
			select.append("\\s*" + extention);
		}
		
		return select.toString();

	}
}
