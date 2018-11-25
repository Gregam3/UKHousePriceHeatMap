package asegroup1.api.daos.location;


import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.LocationData;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@Transactional
@Repository
public class LocationDaoImpl extends DaoImpl<LocationData> {

	private static final String TABLE_NAME = "location_data";

	public LocationDaoImpl() {
		setCurrentClass(LocationData.class);
	}

	@SuppressWarnings("unchecked")
	public List<LocationData> getUserLocations(String userID) {
		return (List<LocationData>) getEntityManager()
				.createNativeQuery("SELECT * FROM " + TABLE_NAME + "\n" + "WHERE USER_ID=:userID").setParameter("userID", userID)
				.getResultList().stream()
				.map(r -> {
					Object[] currentItem = (Object[]) r;

					LocationData locationData = new LocationData();
					locationData.setUserId(String.valueOf(currentItem[0]));
					String time = String.valueOf(currentItem[1]);
					locationData.setTimelog(Timestamp.valueOf(time));

					locationData.setLatitude(Float.valueOf(String.valueOf(currentItem[2])));
					locationData.setLongitude(Float.valueOf(String.valueOf(currentItem[3])));
					locationData.setAltitude(Float.valueOf(String.valueOf(currentItem[4])));

					locationData.setDelivered(Boolean.valueOf(String.valueOf(currentItem[5])));

					return locationData;
				}).collect(Collectors.toList());
	}
}
