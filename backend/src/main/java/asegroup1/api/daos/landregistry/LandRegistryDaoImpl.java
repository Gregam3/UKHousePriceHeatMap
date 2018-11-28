package asegroup1.api.daos.landregistry;

import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.PostCodeCoordinates;
import asegroup1.api.models.landregistry.LandRegistryData;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.stream.Collectors;

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
        throw new AssertionError("Items cannot be deleted from postcodelatlng table");
    }

    @Override
    public List<PostCodeCoordinates> list() {
        throw new AssertionError("All Postcodes cannot be listed due to magnitude, use searchForLandRegistryDataInBoundaries instead.");
    }

    @SuppressWarnings("unchecked")
    public List<LandRegistryData> searchForLandRegistryDataInBoundaries(
            double top,
            double right,
            double bottom,
            double left,
            boolean sorted
    ) {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        double delta = ((top - bottom));

        int scalingModifier = 8 - (int) ((delta > 0) ? delta /2 : delta);

        List<LandRegistryData> collectedResponse = (List<LandRegistryData>) em.createNativeQuery(
                "SELECT postcode, avg(latitude) as avgLat, avg(longitude) as avgLon, averageprice, SUBSTRING(postcode, 1, :scalingModifier) as postcode_aggregate FROM postcodelatlng \n" +
                        "WHERE averageprice > 0 \n" +
                        "GROUP BY postcode_aggregate\n" +
                        "HAVING avgLon > :bottomBound \n" +
                        "AND avgLon < :topBound \n" +
                        "AND avgLat > :leftBound \n" +
                        "AND avgLat < :rightBound \n" +
                        "ORDER BY RAND()\n" +
                        "LIMIT 1000")
                .setParameter("scalingModifier", (scalingModifier > 3) ? scalingModifier : 3)
                .setParameter("topBound", top)
                .setParameter("bottomBound", bottom)
                .setParameter("rightBound", right)
                .setParameter("leftBound", left)
                .getResultList().stream().map(r -> {
                    Object[] currentItem = (Object[]) r;

                    LandRegistryData landRegistryData = new LandRegistryData();
                    landRegistryData.setPostCode(String.valueOf(currentItem[0]));
                    landRegistryData.setLatitude(Double.valueOf(String.valueOf(currentItem[1])));
                    landRegistryData.setLongitude(Double.valueOf(String.valueOf(currentItem[2])));

                    String pricePaid = String.valueOf(currentItem[3]);
                    landRegistryData.setPricePaid(Long.valueOf(pricePaid));

                    return landRegistryData;
                }).collect(Collectors.toList());

        em.close();

        if (sorted) Collections.sort(collectedResponse);

        return collectedResponse;
    }

    public int updateAveragePrice(HashMap<String, Long> averagePrices) {
        int updatedRecords = 0;
        EntityManager em = getEntityManager();

        for (Entry<String, Long> averagePrice : averagePrices.entrySet()) {
            PostCodeCoordinates coordsToUpdate = em.find(PostCodeCoordinates.class, averagePrice.getKey());

            if (!coordsToUpdate.getAverageprice().equals(averagePrice.getValue())) {

                // update local values
                em.getTransaction().begin();
                coordsToUpdate.setAverageprice(averagePrice.getValue());
                em.merge(coordsToUpdate);

                // write update to database
                em.getTransaction().commit();
                updatedRecords++;
            }
        }

        em.close();

        return updatedRecords;
    }

    @SuppressWarnings("unchecked")
    public HashMap<String, List<String>> getMatchingPostcodes(String regex, boolean restrictToUnset, int groupCharSize) {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        List<String> postcodes = (List<String>) em
                .createNativeQuery("SELECT postcode FROM " + TABLE_NAME + "\n" + "WHERE postcode LIKE :outcode" + (restrictToUnset ? " AND averageprice = 0" : ""))
                .setParameter("outcode", regex + "%").getResultList().stream().map(String::valueOf).collect(Collectors.toList());

        HashMap<String, List<String>> postcodeMap = new HashMap<>();

        for (String postcode : postcodes) {
            String localPostcode = postcode.substring(0, postcode.length() - groupCharSize);

            if (!postcodeMap.containsKey(localPostcode)) {
                postcodeMap.put(localPostcode, new ArrayList<>());
            }
            postcodeMap.get(localPostcode).add(postcode);
        }

        em.close();

        return postcodeMap;
    }
}