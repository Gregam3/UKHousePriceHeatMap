package asegroup1.api.services.location;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import asegroup1.api.daos.location.LocationDaoImpl;
import asegroup1.api.models.LocationData;
import asegroup1.api.models.UserData;
import asegroup1.api.services.ServiceImpl;
import asegroup1.api.services.user.UserServiceImpl;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */

@Service
public class LocationServiceImpl extends ServiceImpl<LocationData> {

    private UserServiceImpl userService;
	private LocationDaoImpl locationDao;

    @Autowired
	public LocationServiceImpl(LocationDaoImpl dao, UserServiceImpl userService) {
        super(dao);
        this.userService = userService;
        this.locationDao = dao;
    }

    @Override
    public void create(LocationData location) {
        if (userService.get(location.getUserId()) == null) {
            userService.create(new UserData(location.getUserId()));
        }

        super.create(location);
    }

	public List<LocationData> getLocationData(String userID){
		return locationDao.getUserLocations(userID);
    }
}
