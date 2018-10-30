package asegroup1.api.daos;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.*;
import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

@Transactional
@Repository
public class DaoImpl<T> implements Dao<T> {

    /**
     * A necessary parameter in many of the entityManager's methods
     */
    private Class currentClass;

    protected void setCurrentClass(Class<T> currentClass) {
        this.currentClass = currentClass;
    }

    private static final EntityManagerFactory emFactory;

    static {
        emFactory = Persistence.createEntityManagerFactory("asegroup1.api");
    }

    @PersistenceContext(unitName = "asegroup1.api", type = PersistenceContextType.EXTENDED)
    public static EntityManager getEntityManager() {
        return emFactory.createEntityManager();
    }

    public static void close() {
        emFactory.close();
    }

    @SuppressWarnings("unchecked")
    public T get(String id) {
        checkIfCurrentClassIsValid();
        return (T) getEntityManager().find(currentClass, id);
    }

    //In hibernate, you query tables by there class name. This retrieves that.
    private String retrieveHibernateTableNameFromClassString() {
        String[] classNameSplitIntoPackageNames = currentClass.getName().split("\\.");

        //Get the final element in array e.g. java.lang.Object
        //                                                 ^
        return classNameSplitIntoPackageNames[classNameSplitIntoPackageNames.length - 1];
    }

    public void delete(String id) {
        getEntityManager().remove(get(id));
    }

    public void update(T t) {
        getEntityManager().merge(t);
    }

    @SuppressWarnings("unchecked")
    public List<T> list() {
        checkIfCurrentClassIsValid();
        return getEntityManager().createQuery("from " + retrieveHibernateTableNameFromClassString(), currentClass).getResultList();
    }

    private void checkIfCurrentClassIsValid() {
        if (currentClass == null) {
            throw new AssertionError("In order to use this method you must set the class with setCurrentClass(). " +
                    "E.g. for UserDaoImpl extends DaoImpl<User>, you should have setCurrentClass(User.class) in your constructor.");
        }
    }

    public void add(T t) {
        getEntityManager().persist(t);
    }
}
