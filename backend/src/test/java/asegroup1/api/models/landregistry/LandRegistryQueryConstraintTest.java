package asegroup1.api.models.landregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.Random;
import java.util.stream.Collectors;

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
	 * {@link LandRegistryQueryConstraint#setEqualityConstraint(Selectable, String...)}
	 */
	@Test
	public void setEqualityConstraintSingle() {
		String str;
		for (Selectable s : new Selectable[] { Selectable.county, Selectable.paon, Selectable.saon, Selectable.town }) {
			str = LandRegistryQueryTestUtils.generateRandomString();
			assertTrue(constraint.setEqualityConstraint(s, str));
			assertEquals(str.toUpperCase(), constraint.getEqualityConstraints().getConstraint(s));
		}
	}

	/**
	 * Test method for
	 * {@link LandRegistryQueryConstraint#getRangeConstraint(String, String)}
	 */
	@Test
	public void testGetRangeConstraint() {
		assertNull(constraint.getRangeConstraint("Test", "="));
		constraint.setMaxPricePaid(12);
		constraint.setMinDate(LocalDate.now());
		assertNull(constraint.getRangeConstraint("pricePaid", "="));
		assertNull(constraint.getRangeConstraint("pricePaid", ">"));
		assertEquals(12 + "", constraint.getRangeConstraint("pricePaid", "<").getValue());
	}

	/**
	 * Test method for
	 * {@link LandRegistryQueryConstraint#setEqualityConstraint(Selectable, String...)}
	 */
	@Test
	public void setEqualityConstraintMultiple() {
		String[] postcodes = LandRegistryQueryTestUtils.getPostCodes();
		assertTrue(constraint.setEqualityConstraint(Selectable.postcode, postcodes));

		assertEquals(Arrays.asList(postcodes).stream().map(v -> "\"" + v + "\"").collect(Collectors.toList()), constraint.getValueList(Selectable.postcode.toString()).getValues());
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
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setPostcodeRegex(java.util.ArrayList)}.
	 */
	@Test
	public void testSetPostcodesArrayListOfString() {
		ArrayList<String> postcodes = new ArrayList<String>(Arrays.asList(LandRegistryQueryTestUtils.getPostCodes()));
		constraint.setPostcodeRegex(postcodes);
		assertEquals(postcodes, constraint.getPostcodes());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setPostcodeRegex(java.lang.String[])}.
	 */
	@Test
	public void testSetPostcodesStringArray() {
		String[] postcodes = LandRegistryQueryTestUtils.getPostCodes();
		constraint.setPostcodeRegex(postcodes);
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
		constraint.setPostcodeRegex(LandRegistryQueryTestUtils.getPostCodes());

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryConstraintRegex()));
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
		constraint.setPostcodeRegex(LandRegistryQueryTestUtils.getPostCodes());

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryConstraintRegex()));
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

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryConstraintRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereValues() {
		constraint.setEqualityConstraint(Selectable.postcode, "BN23 7LY", "BN23 7LZ", "BN23 7LG");
		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryConstraintRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereNoFilters() {
		LandRegistryData data = LandRegistryQueryTestUtils.genLandRegistryData();
		constraint = new LandRegistryQueryConstraint(data);

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryConstraintRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereEmpty() {
		constraint = new LandRegistryQueryConstraint();

		assertTrue(constraint.buildQueryContent().matches(LandRegistryQueryTestUtils.buildQueryConstraintRegex()));
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

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint.RangeConstraint#equals(Object)}.
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testRangeConstraintEquals() {
		RangeConstraint[] constraints = new RangeConstraint[6];
		constraints[0] = constraint.new RangeConstraint("type", "name", "=", "value");
		constraints[1] = constraint.new RangeConstraint("type", "name", "=", "value");
		constraints[2] = constraint.new RangeConstraint("type1", "name", "=", "value");
		constraints[3] = constraint.new RangeConstraint("type", "name1", "=", "value");
		constraints[4] = constraint.new RangeConstraint("type", "name", "==", "value");
		constraints[5] = constraint.new RangeConstraint("type", "name", "=", "value1");

		assertTrue(constraints[0].equals(constraints[1]));
		assertFalse(constraints[0].equals(constraints[2]));
		assertFalse(constraints[0].equals(constraints[3]));
		assertFalse(constraints[0].equals(constraints[4]));
		assertTrue(constraints[0].equals(constraints[5]));
		assertFalse(constraints[0].equals("test"));
	}

}
