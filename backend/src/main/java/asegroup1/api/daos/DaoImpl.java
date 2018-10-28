package asegroup1.api.daos;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

public class DaoImpl<T> implements Dao<T> {

    /**
     * A necessary parameter in many of the entityManager's methods
     */
    private Class currentClass;

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

    //In hibernate, you query tables by there class name. This retrieves that.
    private String retrieveHibernateTableNameFromClassString() {
        String[] classNameSplitIntoPackageNames =  currentClass.getName().split("\\.");

        //Get the final element in array e.g. java.lang.Object
        //                                                 ^
        return classNameSplitIntoPackageNames[classNameSplitIntoPackageNames.length - 1];
    }

    public void delete(String id) {
        entityManager.remove(get(id));
    }

    public void update(T t) {
        entityManager.merge(t);
    }

    @SuppressWarnings("unchecked")
    public List<T> list() {
        checkIfCurrentClassIsValid();
        return entityManager.createQuery("from " + retrieveHibernateTableNameFromClassString(), currentClass).getResultList();
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