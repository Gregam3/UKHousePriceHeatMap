package ase.daos;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

@Repository
@Transactional
public class DaoImpl<T> implements Dao<T> {

    /**A necessary parameter in many of the entityManager's methods*/
    private Class currentClass;

    public DaoImpl(T t) {
        this.currentClass = t.getClass();
    }

    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    @SuppressWarnings("unchecked")
    public T get(String id) {
        checkIfCurrentClassIsValid();
        return (T) entityManager.find(currentClass, id);
    }

    public void delete(String id) {
        entityManager.remove(get(id));
    }

    public void update(T t) {
        entityManager.merge(t);
    }

    public List list(String tableName) {
        checkIfCurrentClassIsValid();
        return entityManager.createQuery("from " + tableName, currentClass).getResultList();
    }

    private void checkIfCurrentClassIsValid() {
        if (currentClass == null) {
            throw new AssertionError("In order to use this method you must set the class in constructor. " +
                    "E.g. for UserDaoImpl extends DaoImpl<User>, you should have super(User.class) in your constructor.");
        }
    }

    public void add(T t) {
        entityManager.persist(t);
    }
}