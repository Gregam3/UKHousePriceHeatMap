package asegroup1.api.services.user;

import asegroup1.api.controllers.LandRegistryController;
import asegroup1.api.daos.user.UserDaoImpl;
import asegroup1.api.models.UserData;
import asegroup1.api.services.ServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author Greg Mitten gregoryamitten@gmail.com
 * @author Rikkey Paal
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserData> {

	private UserDaoImpl userDataDao;
	private final static Logger logger = LogManager.getLogger(LandRegistryController.class);


	@Autowired
	public UserServiceImpl(UserDaoImpl dao) {
		super(dao);
		userDataDao = dao;
	}

	public int add(String key) {
		if(userDataDao.get(key) != null) {
			return -1;
		}
		logger.info("Adding: " + key);
		userDataDao.add(new UserData(key));
		return (userDataDao.get(key) == null ? 1 : 0);
	}

}
