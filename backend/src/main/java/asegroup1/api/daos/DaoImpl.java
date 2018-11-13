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
    private Class<T> currentClass;

    protected void setCurrentClass(Class<T> currentClass) {
        this.currentClass = currentClass;
    }
    private static EntityManagerFactory entityManagerFactory;


    //Set up entity manager based on properties provided in application.properties
    @PersistenceContext(type = PersistenceContextType.EXTENDED)
    private EntityManager entityManager;

    //Each time a Dao wants to access the database they fetch a new entity manager
    private EntityManager getEntityManager() {
        //On the first attempt to get an entity manager, a factory is created based on the properties in the template entity manager, entity managers are retrieved from this factory
        if (entityManagerFactory == null)
            entityManagerFactory = entityManager.getEntityManagerFactory();

        return entityManagerFactory.createEntityManager();
    }

    public T get(String id) {
        checkIfCurrentClassIsValid();
        return getEntityManager().find(currentClass, id);
    }

    public void delete(String id) {
        getEntityManager().remove(get(id));
    }

    public void update(T t) {
        getEntityManager().merge(t);
    }

    public List<T> list() {
        checkIfCurrentClassIsValid();
        return getEntityManager().createQuery("from " + currentClass.getSimpleName(), currentClass).getResultList();
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
