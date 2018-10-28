package asegroup1.api.services.user;

import asegroup1.api.daos.Dao;
import asegroup1.api.models.UserData;
import asegroup1.api.services.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserData> {

    private Dao<UserData> userDataDao;

    @Autowired
    public UserServiceImpl(Dao<UserData> dao) {
        super(dao);
    }
}
