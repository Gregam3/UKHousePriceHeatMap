package asegroup1.api.daos.user;


import asegroup1.api.daos.Dao;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;

@Repository
@Transactional
public interface UserDao extends Dao {
}
