package asegroup1.api.services.location;

import java.security.InvalidParameterException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import asegroup1.api.daos.location.LocationDaoImpl;
import asegroup1.api.models.LocationData;
import asegroup1.api.services.ServiceImpl;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@Service
public class LocationServiceImpl extends ServiceImpl<LocationData> {

	private LocationDaoImpl locationDao;

    @Autowired
	public LocationServiceImpl(LocationDaoImpl dao) {
        super(dao);
        this.locationDao = dao;
    }

	@Override
	public void create(LocationData t) {
		if (locationDao.getLocationDataById(t.getUserId(), t.getTimelog()).size() == 0) {
			super.create(t);
		}else {
			throw new InvalidParameterException("Entry already exists");
		}
		
	}

	public List<LocationData> getLocationData(String userID){
		return locationDao.getUserLocations(userID);
    }
}
