package asegroup1.api.daos.landregistry;

import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.PostCodeCoordinates;
import asegroup1.api.models.landregistry.LandRegistryData;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
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
            double left
    ) {
        return (List<LandRegistryData>) getEntityManager().createNativeQuery(
                "SELECT postcode, latitude, longitude FROM " + TABLE_NAME + "\n" +
                        "WHERE latitude > :bottomBound AND latitude < :topBound\n" +
                        "AND longitude > :leftBound AND longitude < :rightBound")
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
                    //TODO get average price when its implemented

                    return landRegistryData;
                }).collect(Collectors.toList());
    }
}