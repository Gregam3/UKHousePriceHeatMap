package asegroup1.api.services.user;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import asegroup1.api.daos.Dao;
import asegroup1.api.models.UserData;
import asegroup1.api.services.ServiceImpl;

@Service
public class UserServiceImpl extends ServiceImpl<UserData> {

	private Dao<UserData> userDataDao;

	@Autowired
	public UserServiceImpl(Dao<UserData> dao) {
		super(dao);
		userDataDao = dao;
	}

	public void generateAndAddNewUserId() {
		String userId = UUID.randomUUID().toString();
		System.out.println("UserID: " + userId);
		userDataDao.add(new UserData(userId));
	}
}
