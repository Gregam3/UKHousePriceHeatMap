package asegroup1.api.models.landregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.RangeConstraint;


/**
 * @author Richousrick
 *
 */
class LandRegistryQueryConstraintTest {

	LandRegistryQueryConstraint constraint;

	@BeforeEach
	public void initConstraint() {
		constraint = new LandRegistryQueryConstraint();
	}

	private void testFiltersEmpty() {
		assertNull(constraint.getRangeConstraint("transactionDate", "<"));
		assertNull(constraint.getRangeConstraint("transactionDate", ">"));
		assertNull(constraint.getRangeConstraint("pricePaid", "<"));
		assertNull(constraint.getRangeConstraint("pricePaid", ">"));
		assertEquals(new ArrayList<>(), constraint.getPostcodes());

	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#LandRegistryQueryConstraint(asegroup1.api.models.landregistry.LandRegistryData)}.
	 */
	@Test
	public void testLandRegistryQueryConstraintLandRegistryData() {
		LandRegistryData data = LandRegistryQueryTestUtils.genLandRegistryData();

		constraint = new LandRegistryQueryConstraint(data);
		assertEquals(data, constraint.getEqualityConstraints());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#LandRegistryQueryConstraint()}.
	 */
	@Test
	public void testLandRegistryQueryConstraint() {
		assertEquals(0, constraint.getEqualityConstraints().getAllConstraints().size());
		testFiltersEmpty();
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setMaxDate(java.time.LocalDate)}.
	 */
	@Test
	public void testSetMaxDate() {
		LocalDate date = LocalDate.now();
		constraint.setMaxDate(date);
		RangeConstraint rangeConstraint = constraint.getRangeConstraint("transactionDate", "<");
		assertNotNull(rangeConstraint);
		assertEquals(date.toString(), rangeConstraint.getValue().replace("\"", ""));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setMinDate(java.time.LocalDate)}.
	 */
	@Test
	public void testSetMinDate() {
		LocalDate date = LocalDate.now();
		constraint.setMinDate(date);
		RangeConstraint rangeConstraint = constraint.getRangeConstraint("transactionDate", ">");
		assertNotNull(rangeConstraint);
		assertEquals(date.toString(), rangeConstraint.getValue().replace("\"", ""));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setMaxPricePaid(int)}.
	 */
	@Test
	public void testSetMaxPricePaid() {
		int pricePaid = new Random(LandRegistryQueryTestUtils.randomSeed).nextInt(Integer.MAX_VALUE);
		constraint.setMaxPricePaid(pricePaid);
		RangeConstraint rangeConstraint = constraint.getRangeConstraint("pricePaid", "<");
		assertNotNull(rangeConstraint);
		assertEquals(pricePaid + "", rangeConstraint.getValue());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setMinPricePaid(int)}.
	 */
	@Test
	public void testSetMinPricePaid() {
		int pricePaid = new Random(LandRegistryQueryTestUtils.randomSeed).nextInt(Integer.MAX_VALUE);
		constraint.setMinPricePaid(pricePaid);
		RangeConstraint rangeConstraint = constraint.getRangeConstraint("pricePaid", ">");
		assertNotNull(rangeConstraint);
		assertEquals(pricePaid + "", rangeConstraint.getValue());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setPostcodes(java.util.ArrayList)}.
	 */
	@Test
	public void testSetPostcodesArrayListOfString() {
		ArrayList<String> postcodes = new ArrayList<String>(Arrays.asList(LandRegistryQueryTestUtils.getRandomPostCodes()));
		constraint.setPostcodes(postcodes);
		assertEquals(postcodes, constraint.getPostcodes());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setPostcodes(java.lang.String[])}.
	 */
	@Test
	public void testSetPostcodesStringArray() {
		String[] postcodes = LandRegistryQueryTestUtils.getRandomPostCodes();
		constraint.setPostcodes(postcodes);
		assertEquals(new ArrayList<String>(Arrays.asList(postcodes)), constraint.getPostcodes());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereComplete() {
		LandRegistryData data = LandRegistryQueryTestUtils.genLandRegistryData();
		constraint = new LandRegistryQueryConstraint(data);
		constraint.setMaxDate(LocalDate.now());
		constraint.setMinPricePaid(20122);
		constraint.setPostcodes(LandRegistryQueryTestUtils.getRandomPostCodes());

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereNoConstraint() {
		constraint = new LandRegistryQueryConstraint();
		constraint.setMaxDate(LocalDate.now());
		constraint.setMinPricePaid(20122);
		constraint.setPostcodes(LandRegistryQueryTestUtils.getRandomPostCodes());

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereNoPostCode() {
		LandRegistryData data = LandRegistryQueryTestUtils.genLandRegistryData();
		constraint = new LandRegistryQueryConstraint(data);
		constraint.setMaxDate(LocalDate.now());
		constraint.setMinPricePaid(20122);

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereNoFilters() {
		LandRegistryData data = LandRegistryQueryTestUtils.genLandRegistryData();
		constraint = new LandRegistryQueryConstraint(data);

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereEmpty() {
		constraint = new LandRegistryQueryConstraint();

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryRegex()));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildUniqueGrouping()}.
	 */
	@Test
	public void testBuildUniqueGrouping() {
		String regex;
		StringBuilder regexBuilder = new StringBuilder("GROUP BY(\\s\\?(");
		EnumSet.allOf(Selectable.class).forEach(v -> {
			regexBuilder.append("(" + v.toString() + ")|");
		});
		regexBuilder.deleteCharAt(regexBuilder.length() - 1);
		regexBuilder.append("))+");
		regex = regexBuilder.toString();

		assertTrue(constraint.buildUniqueGrouping().matches(regex));
	}

}
