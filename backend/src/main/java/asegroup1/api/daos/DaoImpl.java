package asegroup1.api.daos;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

public class DaoImpl<T> {

    /**
     * A necessary parameter in many of the entityManager's methods
     */
    private Class<T> currentClass;

    protected void setCurrentClass(Class<T> currentClass) {
        this.currentClass = currentClass;
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

    public void delete(T t) {
        entityManager.remove(t);
    }

    public void update(T t) {
        entityManager.merge(t);
    }

    @SuppressWarnings("unchecked")
    public List<T> list() {
        checkIfCurrentClassIsValid();
        return entityManager.createQuery("from " + currentClass.getSimpleName(), currentClass).getResultList();
    }

    private void checkIfCurrentClassIsValid() {
        if (currentClass == null) {
            throw new AssertionError("In order to use this method you must set the class with setCurrentClass(). " +
                    "E.g. for UserDaoImpl extends DaoImpl<User>, you should have setCurrentClass(User.class) in your constructor.");
        }
    }

    public void add(T t) {
        entityManager.persist(t);
    }
}