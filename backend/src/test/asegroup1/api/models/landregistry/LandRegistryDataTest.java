/**
 * 
 */
package asegroup1.api.models.landregistry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.security.InvalidParameterException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.EnumSet;
import java.util.Random;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.EstateType;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.PropertyType;
import asegroup1.api.models.landregistry.LandRegistryQueryConstraint.TransactionCategory;
import asegroup1.api.models.landregistry.LandRegistryQuerySelect.Selectable;

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

	static String generateRandomString() {
		return "Random: " + Math.random();
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
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setPrimaryHouseName(java.lang.String)}.
	 */
	@Test
	public void testSetPrimaryHouseName() {
		String houseNameStr = generateRandomString();
		lRData.setPrimaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.paon, houseNameStr);

		houseNameStr = generateRandomString();
		lRData.setPrimaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.paon, houseNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setSecondaryHouseName(java.lang.String)}.
	 */
	@Test
	public void testSetSecondaryHouseName() {
		String houseNameStr = generateRandomString();
		lRData.setSecondaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.saon, houseNameStr);

		houseNameStr = generateRandomString();
		lRData.setSecondaryHouseName(houseNameStr);
		assertStoredStringEqual(Selectable.saon, houseNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setStreetName(java.lang.String)}.
	 */
	@Test
	public void testSetStreetName() {
		String streetNameStr = generateRandomString();
		lRData.setStreetName(streetNameStr);
		assertStoredStringEqual(Selectable.street, streetNameStr);

		streetNameStr = generateRandomString();
		lRData.setStreetName(streetNameStr);
		assertStoredStringEqual(Selectable.street, streetNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setTownName(java.lang.String)}.
	 */
	@Test
	public void testSetTownName() {
		String townNameStr = generateRandomString();
		lRData.setTownName(townNameStr);
		assertStoredStringEqual(Selectable.town, townNameStr);

		townNameStr = generateRandomString();
		lRData.setTownName(townNameStr);
		assertStoredStringEqual(Selectable.town, townNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setLocality(java.lang.String)}.
	 */
	@Test
	public void testSetLocality() {
		String localityNameStr = generateRandomString();
		lRData.setLocality(localityNameStr);
		assertStoredStringEqual(Selectable.locality, localityNameStr);

		localityNameStr = generateRandomString();
		lRData.setLocality(localityNameStr);
		assertStoredStringEqual(Selectable.locality, localityNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setDistrict(java.lang.String)}.
	 */
	@Test
	public void testSetDistrict() {
		String districtNameStr = generateRandomString();
		lRData.setDistrict(districtNameStr);
		assertStoredStringEqual(Selectable.district, districtNameStr);

		districtNameStr = generateRandomString();
		lRData.setDistrict(districtNameStr);
		assertStoredStringEqual(Selectable.district, districtNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setCounty(java.lang.String)}.
	 */
	@Test
	public void testSetCounty() {
		String countyNameStr = generateRandomString();
		lRData.setCounty(countyNameStr);
		assertStoredStringEqual(Selectable.county, countyNameStr);

		countyNameStr = generateRandomString();
		lRData.setCounty(countyNameStr);
		assertStoredStringEqual(Selectable.county, countyNameStr);
	}

	/**
	 * Test method for {@link asegroup1.api.models.landregistry.LandRegistryData#setPostCode(java.lang.String)}.
	 */
	@Test
	public void testSetPostCode() {
		String postCode = "bn21 4nv";

		try {
			lRData.setPostCode(postCode);
			assertStoredStringEqual(Selectable.postcode, postCode);
		} catch (InvalidParameterException e) {
			fail("Did not accpet well formatted string");
		}

		try {
			lRData.setPostCode("bn214nv");
			fail("Did not accpet well formatted string");
		} catch (InvalidParameterException e) {
		}
		assertStoredStringEqual(Selectable.postcode, postCode);
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
	public void testSetConstraint() {
		

		EnumSet.allOf(Selectable.class).forEach(selectable -> {
			String value = null;
			Random rand = new Random();
			switch (selectable) {
				case paon:
				case saon:
				case street:
				case town:
				case locality:
				case district:
				case county:
					for (int i = 0; i < 5; i++) {
						value = generateRandomString();
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

					try {
						lRData.setConstraint(Selectable.postcode.toString(), "bn214nv");
						fail("Did not accpet well formatted string");
					} catch (InvalidParameterException e) {
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
					testSetInvalidConstraint(selectable, "12.2");
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
					fail("Unexpected Selectable type: "+selectable.toString());
			}
			
			
			

		});
		
		
	}

	@Test
	public void testSetConstraintInvalidKey() {
		// test adding value with invalid selectable
		String invalidSelectable = "invalidSelectable";
		try {
			Selectable.valueOf(invalidSelectable);
			fail("\"inValidSelectable\" is resolving to a selectable");
		} catch (IllegalArgumentException e) {
			// expected, as should not be parseable
		}
		try {
			assertFalse(lRData.setConstraint(invalidSelectable, "someData"));
			fail("Should throw illigalArgumentException");
		} catch (IllegalArgumentException e) {
			// expected, as should not be parseable
		}
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

}
