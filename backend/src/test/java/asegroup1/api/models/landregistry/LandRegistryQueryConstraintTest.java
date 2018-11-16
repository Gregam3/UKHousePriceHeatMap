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

import asegroup1.api.models.landregistry.LandRegistryQuery.PropertyType;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.RangeConstraint;


/**
 * @author Richousrick
 *
 */
class LandRegistryQueryConstraintTest {

	LandRegistryQueryConstraint constraint;

	private static long randomSeed = 8312595207343625996L;

	private static String[] getRandomPostCodes() {
		return new String[] { "OX14 1WH", "L18 9SN", "TN27 8JG", "PL8 2EE" };
	}

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

	private static LandRegistryData genLandRegistryData() {
		LandRegistryData data = new LandRegistryData();
		data.setNewBuild(true);
		data.setPricePaid(new Random(randomSeed).nextInt(Integer.MAX_VALUE));
		data.setPrimaryHouseName(LandRegistryDataTest.generateRandomString());
		data.setStreetName(LandRegistryDataTest.generateRandomString());
		data.setTownName(LandRegistryDataTest.generateRandomString());
		data.setPropertyType(PropertyType.terraced);
		return data;
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#LandRegistryQueryConstraint(asegroup1.api.models.landregistry.LandRegistryData)}.
	 */
	@Test
	public void testLandRegistryQueryConstraintLandRegistryData() {
		LandRegistryData data = genLandRegistryData();

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
		int pricePaid = new Random(randomSeed).nextInt(Integer.MAX_VALUE);
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
		int pricePaid = new Random(randomSeed).nextInt(Integer.MAX_VALUE);
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
		ArrayList<String> postcodes = new ArrayList<String>(Arrays.asList(getRandomPostCodes()));
		constraint.setPostcodes(postcodes);
		assertEquals(postcodes, constraint.getPostcodes());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#setPostcodes(java.lang.String[])}.
	 */
	@Test
	public void testSetPostcodesStringArray() {
		String[] postcodes = getRandomPostCodes();
		constraint.setPostcodes(postcodes);
		assertEquals(new ArrayList<String>(Arrays.asList(postcodes)), constraint.getPostcodes());
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereComplete() {
		LandRegistryData data = genLandRegistryData();
		constraint = new LandRegistryQueryConstraint(data);
		constraint.setMaxDate(LocalDate.now());
		constraint.setMinPricePaid(20122);
		constraint.setPostcodes(getRandomPostCodes());

		assertTrue(constraint.buildQueryContent().matches(buildQueryRegex()));
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
		constraint.setPostcodes(getRandomPostCodes());

		assertTrue(constraint.buildQueryContent().matches(buildQueryRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereNoPostCode() {
		LandRegistryData data = genLandRegistryData();
		constraint = new LandRegistryQueryConstraint(data);
		constraint.setMaxDate(LocalDate.now());
		constraint.setMinPricePaid(20122);

		assertTrue(constraint.buildQueryContent().matches(buildQueryRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereNoFilters() {
		LandRegistryData data = genLandRegistryData();
		constraint = new LandRegistryQueryConstraint(data);

		assertTrue(constraint.buildQueryContent().matches(buildQueryRegex()));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryQueryConstraint#buildQueryContent()}.
	 */
	@Test
	public void testBuildQueryWhereEmpty() {
		constraint = new LandRegistryQueryConstraint();

		assertTrue(constraint.buildQueryContent().matches(buildQueryRegex()));
	}

	private String buildQueryRegex() {
		String delimeter = "\\s*";
		// value regex parts
		String valueReference = "\\?\\w+";
		String valueString = "\"[^\"]*\"";
		String valueInteger = "\\d+";
		String valueBoolean = regexOptionalList("true", "false");

		String yearReg = "\\d{4}";
		String monthReg = regexOptionalList("0[1-9]", "1[0-2]");
		String dayReg = regexOptionalList("0[1-9]", "[1-2]\\d", "3[0,1]");
		String valueCalendar = regexAddWithDelim("\\-", yearReg, monthReg, dayReg);
		
		// declaration segments
		String namespace = "\\w+:\\w+";
		String value = regexOptionalList(valueReference, valueString, valueInteger, valueCalendar, namespace, valueBoolean);
		String advancedNameSpace = namespace + "(/" + namespace + ")?";

		// declaration
		String partialDeclaration = regexAddWithDelim(delimeter, advancedNameSpace, value);
		

		String partialDeclarationListStart = regexAddWithDelim(delimeter, partialDeclaration, ";");
		String partialDeclarationList = regexAddWithDelim(delimeter, "(", partialDeclarationListStart, ")*", partialDeclaration);




		String declaration = regexAddWithDelim("\\s+", valueReference, partialDeclarationList);
		String fullDeclaration = regexAddWithDelim(delimeter, declaration, "\\.");

		String optionalDeclaration = regexAddWithDelim(delimeter, "OPTIONAL", "\\{", declaration, "\\}");



		// declaration list
		
		String declarationListOptions = regexOptionalList(fullDeclaration, optionalDeclaration);
		String declarationList = regexAddWithDelim(delimeter, "(", declarationListOptions, ")+");


		//FILTER
		
		// range filter
		String filterConstraint = "[<>]";
		String filterCast = "\\^\\^"+namespace;
		String filterValue = value + "(" + filterCast + ")?";
		String rangeFilter = regexAddWithDelim(delimeter, valueReference, filterConstraint, filterValue);
		
		// regex filter
		String regexFilterValue = "\".*\"";
		String regexFilter = regexAddWithDelim(delimeter, "REGEX", "\\(", valueReference, ",", regexFilterValue, "\\)");

		String filterOption = regexOptionalList(rangeFilter, regexFilter);
		String filterOptionListStart = regexAddWithDelim(delimeter, "(", filterOption, "&&)*");
		String filterOptionList = regexAddWithDelim(delimeter, filterOptionListStart, filterOption);
		
		String filter = regexAddWithDelim(delimeter, "FILTER", "\\(", "(" + filterOptionList + ")?", "\\)");
		String optionalFilter = "(" + filter + ")?";

		String queryRegex = regexAddWithDelim(delimeter, "WHERE", "\\{", declarationList, optionalFilter, "\\}");

		
		return queryRegex;
	}

	private static String regexAddWithDelim(String delimeter, String... parts) {
		StringBuilder str = new StringBuilder();
		for (int i = 0; i < parts.length - 1; i++) {
			str.append(parts[i] + delimeter);
		}
		str.append(parts[parts.length - 1]);
		return str.toString();
	}

	private static String regexOptionalList(String... options) {
		StringBuilder str = new StringBuilder("(");
		for (String option : options) {
			str.append("(" + option + ")|");
		}
		str.deleteCharAt(str.length() - 1);
		str.append(")");
		return str.toString();
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
