package asegroup1.api.services.location;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import asegroup1.api.daos.Dao;
import asegroup1.api.models.LocationData;
import asegroup1.api.models.UserData;
import asegroup1.api.services.ServiceImpl;
import asegroup1.api.services.user.UserServiceImpl;

@Service
public class LocationServiceImpl extends ServiceImpl<LocationData> {

	private UserServiceImpl userService;
	private Dao<LocationData> locDao;

	@Autowired
	public LocationServiceImpl(Dao<LocationData> dao, UserServiceImpl userService) {
		super(dao);
		this.userService = userService;
		this.locDao = dao;
	}


	@Override
	public void create(LocationData location) {
		if (userService.get(location.getUserId()) == null) {
			userService.create(new UserData(location.getUserId()));
		}
		super.create(location);
	}
}
