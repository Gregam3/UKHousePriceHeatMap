package asegroup1.api.services.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import asegroup1.api.daos.user.UserDaoImpl;
import asegroup1.api.models.UserData;
import asegroup1.api.services.ServiceImpl;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserData> {

	private UserDaoImpl userDataDao;

	@Autowired
	public UserServiceImpl(UserDaoImpl dao) {
		super(dao);
		userDataDao = dao;
	}

	public int add(String key) {
		if(userDataDao.get(key) != null) {
			return -1;
		}
		System.out.println("Adding: " + key);
		userDataDao.add(new UserData(key));
		return (userDataDao.get(key) == null ? 1 : 0);
	}

}
