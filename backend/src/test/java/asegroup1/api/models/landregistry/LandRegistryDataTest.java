/**
 * 
 */
package asegroup1.api.models.landregistry;

import asegroup1.api.models.heatmap.Colour;
import asegroup1.api.models.landregistry.LandRegistryData.AddrConstraint;
import asegroup1.api.models.landregistry.LandRegistryData.TransConstraint;
import asegroup1.api.models.landregistry.LandRegistryQuery.EstateType;
import asegroup1.api.models.landregistry.LandRegistryQuery.PropertyType;
import asegroup1.api.models.landregistry.LandRegistryQuery.Selectable;
import asegroup1.api.models.landregistry.LandRegistryQuery.TransactionCategory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

/**
 * @author Richousrick
 *
 */
class LandRegistryDataTest {

	private LandRegistryData lRData;

	@BeforeEach
	public void initLandRegistryData() {
		lRData = new LandRegistryData();
	}

	private void assertStoredStringEqual(Selectable selectable, String expected) {
		assertEquals(expected.toUpperCase(), lRData.getConstraint(selectable));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#LandRegistryData()}.
	 */
	@Test
	public void testLandRegistryData() {
		assertNotNull(lRData.getAllConstraints());
		assertTrue(lRData.getAllConstraints().isEmpty());

		// check all values equate to null
		EnumSet.allOf(Selectable.class).forEach(v -> assertNull(lRData.getConstraint(v)));

	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setColour(Colour)}
	 * and {@link asegroup1.api.models.landregistry.LandRegistryData#getColour()}.
	 */
	@Test
	public void testSetGetColour() {
		Colour c = new Colour(70);
		assertNull(lRData.getColour());
		lRData.setColour(c);
		assertEquals(c, lRData.getColour());
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#getId()}.
	 */
	@Test
	public void testGetID() {
		assertTrue(lRData.getId().matches("\\b[0-9a-f]{8}\\b-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-\\b[0-9a-f]{12}\\b"));
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#getConstraintNotNull(Selectable)}.
	 */
	@Test
	public void testGetConstraintNotNull() {
		Selectable sel = Selectable.paon;
		String newVal = "test";

		assertNull(lRData.getConstraint(sel));
		assertEquals("", lRData.getConstraintNotNull(sel));
		lRData.setConstraint(sel + "", newVal);
		assertEquals(newVal.toUpperCase(), lRData.getConstraintNotNull(sel));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#removeConstraint(Selectable)}.
	 */
	@Test
	public void testRemoveConstraint() {
		Selectable sel = Selectable.paon;
		String tmpValue = "test";

		assertNull(lRData.getConstraint(sel));
		assertFalse(lRData.removeConstraint(sel));

		lRData.setConstraint(sel + "", tmpValue);
		assertNotNull(lRData.getConstraint(sel));
		assertTrue(lRData.removeConstraint(sel));
		assertNull(lRData.getConstraint(sel));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setPrimaryHouseName(java.lang.String)}.
	 */
	@Test
	public void testSetPrimaryHouseName() {
		String houseNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setPrimaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.paon, houseNameStr);

		houseNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setPrimaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.paon, houseNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setSecondaryHouseName(java.lang.String)}.
	 */
	@Test
	public void testSetSecondaryHouseName() {
		String houseNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setSecondaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.saon, houseNameStr);

		houseNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setSecondaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.saon, houseNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setStreetName(java.lang.String)}.
	 */
	@Test
	public void testSetStreetName() {
		String streetNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setStreetName(streetNameStr);
		assertStoredStringEqual(Selectable.street, streetNameStr);

		streetNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setStreetName(streetNameStr);
		assertStoredStringEqual(Selectable.street, streetNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setTownName(java.lang.String)}.
	 */
	@Test
	public void testSetTownName() {
		String townNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setTownName(townNameStr);
		assertStoredStringEqual(Selectable.town, townNameStr);

		townNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setTownName(townNameStr);
		assertStoredStringEqual(Selectable.town, townNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setLocality(java.lang.String)}.
	 */
	@Test
	public void testSetLocality() {
		String localityNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setLocality(localityNameStr);
		assertStoredStringEqual(Selectable.locality, localityNameStr);

		localityNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setLocality(localityNameStr);
		assertStoredStringEqual(Selectable.locality, localityNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setDistrict(java.lang.String)}.
	 */
	@Test
	public void testSetDistrict() {
		String districtNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setDistrict(districtNameStr);
		assertStoredStringEqual(Selectable.district, districtNameStr);

		districtNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setDistrict(districtNameStr);
		assertStoredStringEqual(Selectable.district, districtNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setCounty(java.lang.String)}.
	 */
	@Test
	public void testSetCounty() {
		String countyNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setCounty(countyNameStr);
		assertStoredStringEqual(Selectable.county, countyNameStr);

		countyNameStr = LandRegistryQueryTestUtils.generateRandomString();
		lRData.setCounty(countyNameStr);
		assertStoredStringEqual(Selectable.county, countyNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setPostCode(java.lang.String)}.
	 */
	public void testSetValidPostCode(String postCode) {
		try {
			lRData.setPostCode(postCode);
			assertStoredStringEqual(Selectable.postcode, postCode);
		} catch (InvalidParameterException e) {
			fail("Did not accept well formatted string");
		}

		assertStoredStringEqual(Selectable.postcode, postCode);
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setPostCode(java.lang.String)}.
	 */
	public void testSetInvalidPostCode(String postCode) {
		assertThrows(InvalidParameterException.class, () -> lRData.setPostCode(postCode));
	}



	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setPropertyType(asegroup1.api.models.landregistry.LandRegistryQueryConstraint.PropertyType)}.
	 */
	@Test
	public void testSetPropertyType() {
		EnumSet.allOf(PropertyType.class).forEach(propertyType -> {
			lRData.setPropertyType(propertyType);
			assertEquals(propertyType.toString(), lRData.getConstraint(Selectable.propertyType));
		});
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setNewBuild(java.lang.Boolean)}.
	 */
	@Test
	public void testSetNewBuild() {
		Boolean newBuild = true;
		lRData.setNewBuild(newBuild);
		assertEquals(newBuild.toString(), lRData.getConstraint(Selectable.newBuild));

		newBuild = false;
		lRData.setNewBuild(newBuild);
		assertEquals(newBuild.toString(), lRData.getConstraint(Selectable.newBuild));
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setEstateType(asegroup1.api.models.landregistry.LandRegistryQueryConstraint.EstateType)}.
	 */
	@Test
	public void testSetEstateType() {
		EnumSet.allOf(EstateType.class).forEach(estateType -> {
			lRData.setEstateType(estateType);
			assertEquals(estateType.toString(), lRData.getConstraint(Selectable.estateType));
		});
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setTransactionCategory(asegroup1.api.models.landregistry.LandRegistryQueryConstraint.TransactionCategory)}.
	 */
	@Test
	public void testSetTransactionCategory() {
		EnumSet.allOf(TransactionCategory.class).forEach(transactionCategory -> {
			lRData.setTransactionCategory(transactionCategory);
			assertEquals(transactionCategory.toString(), lRData.getConstraint(Selectable.transactionCategory));
		});
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setPricePaid(int)}.
	 */
	@Test
	public void testSetPricePaid() {
		Random rand = new Random();
		int num = 0;
		for (int i = 0; i < 5; i++) {
			num = rand.nextInt(Integer.MAX_VALUE);
			lRData.setPricePaid(num);
			assertEquals(num + "", lRData.getConstraint(Selectable.pricePaid));
		}
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setTransactionDate(java.time.LocalDate)}.
	 */
	@Test
	public void testSetTransactionDate() {
		Random rand = new Random();
		LocalDate expectedDate, actualDate;
		for (int i = 0; i < 5; i++) {
			expectedDate = LocalDate.now().minusDays(rand.nextInt(7000));
			lRData.setTransactionDate(expectedDate);
			try {
				actualDate = LocalDate.parse(lRData.getConstraint(Selectable.transactionDate));
				assertEquals(expectedDate, actualDate);
			} catch (DateTimeParseException e) {
				fail("Stored LocalDate \"" + lRData.getConstraint(Selectable.transactionDate) + "\"could not be parsed", e);
			}
		}
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setConstraint(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSetValidConstraint() {
		EnumSet.allOf(Selectable.class).forEach(selectable -> {
			String value = null;
			Random rand = new Random(LandRegistryQueryTestUtils.randomSeed);
			switch (selectable) {
				case paon:
				case saon:
				case street:
				case town:
				case locality:
				case district:
				case county:
					for (int i = 0; i < 5; i++) {
						value = LandRegistryQueryTestUtils.generateRandomString();
						testSetConstraint(selectable, value.toUpperCase());
					}
					break;
				case postcode:
					String postCode = "bn21 4nv";

					try {
						lRData.setConstraint(Selectable.postcode.toString(), postCode);
						assertStoredStringEqual(Selectable.postcode, postCode);
					} catch (InvalidParameterException e) {
						fail("Did not accpet well formatted string");
					}

					assertStoredStringEqual(Selectable.postcode, postCode);
					break;
				case newBuild:
					testSetConstraint(selectable, "true");
					testSetConstraint(selectable, "false");
					testSetInvalidConstraint(selectable, "nope");
					break;
				case propertyType:
					EnumSet.allOf(PropertyType.class).forEach(propertyType -> {
						testSetConstraint(selectable, propertyType.toString());
					});
					testSetInvalidConstraint(selectable, "nope");
					break;
				case estateType:
					EnumSet.allOf(EstateType.class).forEach(estateType -> {
						testSetConstraint(selectable, estateType.toString());
					});
					testSetInvalidConstraint(selectable, "nope");
					break;
				case transactionCategory:
					EnumSet.allOf(TransactionCategory.class).forEach(transactionCategory -> {
						testSetConstraint(selectable, transactionCategory.toString());
					});
					testSetInvalidConstraint(selectable, "nope");
					break;
				case pricePaid:
					for (int i = 0; i < 5; i++) {
						value = rand.nextInt(Integer.MAX_VALUE) + "";
						testSetConstraint(selectable, value);
					}
					testSetInvalidConstraint(selectable, "words");
					break;
				case transactionDate:
					LocalDate randomDate;
					for (int i = 0; i < 5; i++) {
						randomDate = LocalDate.now().minusDays(rand.nextInt(7000));
						testSetConstraint(selectable, randomDate.toString());
					}
					// test strings
					testSetInvalidConstraint(selectable, "nope");
					testSetInvalidConstraint(selectable, "not-a-date");
					// wrong order
					testSetInvalidConstraint(selectable, "11-12-2018");
					testSetInvalidConstraint(selectable, "2018-14-10");
					// missing digits
					testSetInvalidConstraint(selectable, "2018-1-10");
					testSetInvalidConstraint(selectable, "2018-10-1");
					break;
				default:
					assertThrows(IllegalArgumentException.class, () -> lRData.setConstraint(selectable + "", "test"));
			}
		});
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setConstraint(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSetInvalidConstraint() {
		assertThrows(IllegalArgumentException.class, () -> lRData.setConstraint("", "test"));
		assertThrows(IllegalArgumentException.class, () -> lRData.setConstraint("a", "test"));
		// test adding value with invalid selectable
		String invalidSelectable = "invalidSelectable";
		assertThrows(IllegalArgumentException.class, () -> Selectable.valueOf(invalidSelectable));
		assertThrows(IllegalArgumentException.class, () -> lRData.setConstraint(invalidSelectable, "someData"));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setConstraint(java.lang.String, java.lang.String)}.
	 */
	@Test
	public void testSetWipeConstraint() {
		Selectable sel = Selectable.paon;
		String tmpValue = "test";

		lRData.setConstraint(sel + "", tmpValue);
		assertNotNull(lRData.getConstraint(sel));
		assertTrue(lRData.setConstraint(sel + "", ""));
		assertNull(lRData.getConstraint(sel));
	}

	private void testSetConstraint(Selectable selectable, String value) {
		assertTrue(lRData.setConstraint(selectable.toString(), value));
		assertEquals(value, lRData.getConstraint(selectable));

		// check handles capitalisation of Selectable (used when filtering by unique
		// values)
		String capitalised = selectable.toString();
		capitalised = capitalised.substring(0, 1).toUpperCase() + capitalised.substring(1);
		assertTrue(lRData.setConstraint(capitalised, value));
		assertEquals(value, lRData.getConstraint(selectable));

	}

	private void testSetInvalidConstraint(Selectable selectable, String value) {
		String oldValue = lRData.getConstraint(selectable);
		assertNotEquals(value, oldValue);
		assertFalse(lRData.setConstraint(selectable.toString(), value));

		// check value was not updated
		assertEquals(oldValue, lRData.getConstraint(selectable));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#getLatitude()} and
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setLatitude(double)}.
	 */
	@Test
	public void testSetGetLatitude() {
		double newLat = 1021341.1235435D;
		assertNull(lRData.getLatitude());
		lRData.setLatitude(newLat);
		assertEquals(new Double(newLat), lRData.getLatitude());
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#getLongitude()} and
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setLongitude(double)}.
	 */
	@Test
	public void testSetGetLongitude() {
		double newLng = 1021341.1235435D;
		assertNull(lRData.getLongitude());
		lRData.setLongitude(newLng);
		assertEquals(new Double(newLng), lRData.getLongitude());
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#getRadius()} and
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setRadius(double)}.
	 */
	@Test
	public void testSetGetRadius() {
		double newRad = 1021341.1235435D;
		assertNull(lRData.getRadius());
		lRData.setRadius(newRad);
		assertEquals(new Double(newRad), lRData.getRadius());
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#setConstraintVar(Selectable,
	 * String))}.
	 */
	@Test
	public void testSetConstraintVar() {
		EnumSet.allOf(Selectable.class).forEach(selectable -> {
			lRData.setConstraintVar(selectable, selectable+"s");
			String expected = null;
			switch(selectable) {
				case paon:
				case saon:
				case street:
				case locality:
				case town:
				case district:
				case county:
				case postcode:
				case transactionDate:
					expected = "?addr lrcommon:%1$s ?%1$ss.";
					break;
				case propertyType:
				case estateType:
					expected = "?transx lrppi:%1$s/skos:prefLabel lrcommon:?%1$ss.";
					break;
				case transactionCategory:
					expected = "?transx lrppi:%1$s/skos:prefLabel lrppi:?%1$ss.";
					break;
				case newBuild:
				case pricePaid:
					expected = "?transx lrppi:%1$s ?%1$ss.";
					break;
				default:
					fail("Illegal argument exception should have been thrown");
			}
			assertEquals(String.format(expected, selectable.toString()), lRData.getEqualityConstraint(selectable).toString());
			
		});
		
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#processConstraintList(Selectable, String...)}.
	 */
	@Test
	public void testProcessConstraintList() {
		EnumSet.allOf(Selectable.class).forEach(selectable -> {
			String[] examples = new String[] { "test1", "test2", "test3" };
			List<String> results = LandRegistryData.processConstraintList(selectable, examples);
			String expectedFormat = null;
			switch (selectable) {
				case paon:
				case saon:
				case street:
				case locality:
				case town:
				case district:
				case county:
				case postcode:
				case transactionDate:
					expectedFormat = "\"%1$S\"";
					break;
				case propertyType:
				case estateType:
					expectedFormat = "lrcommon:%1$s";
					break;
				case transactionCategory:
					expectedFormat = "lrppi:%1$s";
					break;
				case newBuild:
				case pricePaid:
					expectedFormat = "%1$s";
					break;
				default:
					fail("Illegal argument exception should have been thrown");
			}

			assertEquals(examples.length, results.size());
			for (int i = 0; i < examples.length; i++) {
				assertEquals(String.format(expectedFormat, examples[i]), results.get(i));
			}

		});
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#equals(Object)}.
	 */
	@Test
	public void testEqualsDfferentValue() {
		String newVal = "test";
		Selectable selectable = Selectable.town;
		LandRegistryData d1 = LandRegistryQueryTestUtils.genLandRegistryData();
		LandRegistryData d2 = LandRegistryQueryTestUtils.genLandRegistryData();
		assertEquals(d1, d2);
		assertNotEquals(newVal, d2.getConstraint(selectable));
		d1.setConstraint(selectable + "", newVal);
		assertNotEquals(d1, d2);
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#equals(Object)}.
	 */
	@Test
	public void testEqualsMissingValue() {
		Selectable selectable = Selectable.town;
		LandRegistryData d1 = LandRegistryQueryTestUtils.genLandRegistryData();
		LandRegistryData d2 = LandRegistryQueryTestUtils.genLandRegistryData();
		assertEquals(d1, d2);
		assertNotNull(d2.getConstraint(selectable));
		d1.removeConstraint(selectable);
		assertNotEquals(d1, d2);
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#compareTo(Object)}.
	 */
	@Test
	public void testCompare() {
		LandRegistryData d1 = LandRegistryQueryTestUtils.genLandRegistryData();
		LandRegistryData d2 = LandRegistryQueryTestUtils.genLandRegistryData();
		assertEquals(0, d1.compareTo(d2));
		d1.setConstraint(Selectable.pricePaid + "", d1.getConstraint(Selectable.pricePaid) + "1");
		assertEquals(1, d1.compareTo(d2));

		assertEquals(-1, d1.compareTo("Test"));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData#equals(Object)}.
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEqualsWrongType() {
		LandRegistryData d = LandRegistryQueryTestUtils.genLandRegistryData();
		assertFalse(d.equals("Wrong type"));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData.AddrConstraint#equals(Object)}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryData.AddrConstraint#compareTo(Object)}.
	 */
	@Test
	public void testAddrConstraintCompare() {
		AddrConstraint addr = lRData.new AddrConstraint("test", "Test", true);
		AddrConstraint addr1 = lRData.new AddrConstraint("test", "Test", true);
		assertEquals(0, addr.compareTo(addr1));

		addr1 = lRData.new AddrConstraint("test1", "Test", true);
		assertEquals(1, addr.compareTo(addr1));


		TransConstraint trans = lRData.new TransConstraint("test", "Test", true);
		assertEquals(-1, addr.compareTo(trans));
	}

	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData.TransConstraint#equals(Object)}
	 * and
	 * {@link asegroup1.api.models.landregistry.LandRegistryData.TransConstraint#compareTo(Object)}.
	 */
	@Test
	public void testTransConstraintCompare() {
		TransConstraint addr = lRData.new TransConstraint("test", "Test", true);
		TransConstraint addr1 = lRData.new TransConstraint("test", "Test", true);
		assertEquals(0, addr.compareTo(addr1));

		addr1 = lRData.new TransConstraint("test1", "Test", true);
		assertEquals(-1, addr.compareTo(addr1));


		AddrConstraint trans = lRData.new AddrConstraint("test", "Test", true);
		assertEquals(1, addr.compareTo(trans));
	}


	/**
	 * Test method for
	 * {@link asegroup1.api.models.landregistry.LandRegistryData.EqualityConstraint#equals(Object)}
	 * and
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Test
	public void testEqualityConstraintEqualsWrongType() {
		assertFalse(lRData.new TransConstraint("test", "Test", true).equals("Test"));
	}

}
