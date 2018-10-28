package asegroup1.api.daos.location;


import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.UserData;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Transactional
@Repository
public class LocationDaoImpl extends DaoImpl<LocationData> {
	public LocationDaoImpl() {
		setCurrentClass(LocationData.class);
	}
}
