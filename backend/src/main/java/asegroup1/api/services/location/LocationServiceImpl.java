package asegroup1.api.services.location;

import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.LocationData;
import asegroup1.api.services.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl extends ServiceImpl<LocationData> {

	@Autowired
	public LocationServiceImpl(DaoImpl<LocationData> dao) {
		super(dao);
	}
}
