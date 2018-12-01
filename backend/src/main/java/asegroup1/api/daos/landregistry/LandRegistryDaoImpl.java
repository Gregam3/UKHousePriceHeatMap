package asegroup1.api.daos.landregistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.stereotype.Repository;

import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.PostCodeCoordinates;
import asegroup1.api.models.landregistry.LandRegistryData;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@Repository
@Transactional
public class LandRegistryDaoImpl extends DaoImpl<PostCodeCoordinates> {


	private static final String TABLE_NAME = "postcodelatlng";

	public LandRegistryDaoImpl() {
		setCurrentClass(PostCodeCoordinates.class);
	}

	@Override
	public void delete(String id) {
		throw new AssertionError(
				"Items cannot be deleted from postcodelatlng table");
	}

	@Override
	public List<PostCodeCoordinates> list() {
		throw new AssertionError(
				"All Postcodes cannot be listed due to magnitude, use searchForLandRegistryDataInBoundaries instead.");
	}

	@SuppressWarnings("unchecked")
	public List<LandRegistryData> searchForLandRegistryDataInBoundaries(
			double top, double right, double bottom, double left,
			boolean sorted) {
		return makeTransaction(em -> {
			double delta = ((top - bottom));
			double deltab3 = Math.log(delta) / Math.log(3);
			int scalingModifier = (int) Math.max(0, Math.min(Math.ceil(deltab3), 3));
			int retCount = 1000;

			List<LandRegistryData> collectedResponse = (List<LandRegistryData>) em
					.createNativeQuery(
							"SELECT SUBSTRING(postcode, 1, 8 "
									+ "- ((5 - Locate(' ', postcode))) "
									+ "- LEAST(3, FLOOR(LOG10(FOUND_ROWS()/"
									+ " :aggregationDiff )) + :scalingModifier ))"
									+ " as postcode_aggregate,"
									+ "avg(latitude) as avgLat, avg(longitude)"
									+ " as avgLon,"
									+ " avg(averageprice) as avgPrice,"
									+ "SQRT(POW(max(latitude)- min(latitude),"
									+ " 2)"
									+ " + POW(max(longitude)- min(longitude),2"
									+ "))*55556 as radius "
									+ "FROM ( SELECT * FROM " + TABLE_NAME + " "
									+ "WHERE averageprice > 0 "
									+ "AND longitude > :bottomBound "
									+ "AND longitude < :topBound "
									+ "AND latitude > :leftBound "
									+ "AND latitude < :rightBound ) as innerQuery "
									+ "group by postcode_aggregate "
									+ "ORDER BY RAND() "
									+ "LIMIT :returnCount")
					.setParameter("scalingModifier", scalingModifier)
					.setParameter("topBound", top)
					.setParameter("bottomBound", bottom)
					.setParameter("rightBound", right)
					.setParameter("leftBound", left)
					.setParameter("aggregationDiff", retCount * 5)
					.setParameter("returnCount", retCount)
					.getResultList().stream()
					.map(r -> extractDataLandRegistryDataFromRow((Object[]) r))
					.collect(Collectors.toList());
			if (sorted)
				Collections.sort(collectedResponse);

			return collectedResponse;
		});
	}

	private LandRegistryData extractDataLandRegistryDataFromRow(Object[] elements) {
		LandRegistryData landRegistryData = new LandRegistryData();
		landRegistryData.setPostCode(String.valueOf(elements[0]));
		landRegistryData
				.setLatitude(Double.valueOf(String.valueOf(elements[1])));
		landRegistryData
				.setLongitude(Double.valueOf(String.valueOf(elements[2])));
		String pricePaid = String.valueOf(elements[3]);
		landRegistryData.setPricePaid(Math.round(Double.valueOf(pricePaid)));
		landRegistryData.setRadius(Double.valueOf(String.valueOf(elements[4])));

		return landRegistryData;
	}

	public int updateAveragePrice(HashMap<String, Long> averagePrices) {

		return makeTransaction(em -> {
			int updatedRecords = 0;
			for (Entry<String, Long> averagePrice : averagePrices.entrySet()) {

				PostCodeCoordinates coordsToUpdate = em
						.find(PostCodeCoordinates.class, averagePrice.getKey());

				if (!coordsToUpdate.getAverageprice()
						.equals(averagePrice.getValue())) {
					coordsToUpdate.setAverageprice(averagePrice.getValue());
					em.merge(coordsToUpdate);
					updatedRecords++;
				}
			}
			return updatedRecords;
		});

	}

	@SuppressWarnings("unchecked")
	public HashMap<String, List<String>> getMatchingPostcodes(String regex,
			boolean restrictToUnset, int groupCharSize) {
		return makeTransaction(em -> {
			List<String> postcodes = (List<String>) em
					.createNativeQuery("SELECT postcode FROM " + TABLE_NAME
							+ "\n" + "WHERE postcode LIKE :outcode"
							+ (restrictToUnset ? " AND averageprice = 0" : ""))
					.setParameter("outcode", regex + "%").getResultList()
					.stream().map(String::valueOf).collect(Collectors.toList());

			HashMap<String, List<String>> postcodeMap = new HashMap<>();

			for (String postcode : postcodes) {
				String localPostcode = postcode.substring(0,
						postcode.length() - groupCharSize);

				if (!postcodeMap.containsKey(localPostcode)) {
					postcodeMap.put(localPostcode, new ArrayList<>());
				}
				postcodeMap.get(localPostcode).add(postcode);
			}
			return postcodeMap;
		});
	}

	public JSONObject getGeoLocationData(String constraintQuery)
			throws UnirestException {
		return Unirest.get(constraintQuery).asJson().getBody().getArray()
				.getJSONObject(0).getJSONArray("results").getJSONObject(0)
				.getJSONObject("geometry").getJSONObject("location");
	}
}