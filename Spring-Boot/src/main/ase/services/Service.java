package ase.services;

import ase.daos.Dao;

import java.util.List;

/**
 * @author Greg Mitten (i7676925)
 * gregoryamitten@gmail.com
 */

//Component annotations do not extend to child classes
public class Service<T> {
    private Dao<T> dao;

    public Service(Dao<T> dao) {
        this.dao = dao;
    }

    public T get(String id) {
        checkIfDaoIsValid();
        return dao.get(id);
    }

    public void update(T t) {
        checkIfDaoIsValid();
        dao.update(t);
    }

    public void delete(String id) {
        checkIfDaoIsValid();
        dao.delete(id);
    }

    public List<T> list() {
        checkIfDaoIsValid();
        return dao.list();
    }

    private void checkIfDaoIsValid() {
        if (dao == null) {
            throw new AssertionError("In order to use this method you must set the class in constructor. " +
                    "E.g. for UserService extends Service<User>, you should put super(userDao) in your constructor." +
                    "If userDao does not yet exist you must create it and then @Autowire it to give it its state");
        }
    }
}