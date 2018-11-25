package asegroup1.api.daos.user;


import javax.transaction.Transactional;

import org.springframework.stereotype.Repository;

import asegroup1.api.daos.DaoImpl;
import asegroup1.api.models.UserData;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

@Transactional
@Repository
public class UserDaoImpl extends DaoImpl<UserData> {
	public UserDaoImpl() {
		setCurrentClass(UserData.class);
	}

}
