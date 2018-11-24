package asegroup1.api.daos;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    protected EntityManager getEntityManager() {
        //On the first attempt to get an entity manager, a factory is created based on the properties in the template entity manager, entity managers are retrieved from this factory
        if (entityManagerFactory == null)
            entityManagerFactory = entityManager.getEntityManagerFactory();

        //The EntityManager will automatically be flushed on transaction completion
        return entityManagerFactory.createEntityManager();
    }

    public T get(String id) {
        EntityManager em = getEntityManager();

        checkIfCurrentClassIsValid();
        em.getTransaction().begin();

        T t = em.find(currentClass, id);

        em.close();

        return t;
    }

    public void delete(String id) {
        EntityManager em = getEntityManager();
        em.getTransaction().begin();

        em.remove(get(id));

        em.close();
    }

    public void update(T t) {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        em.merge(t);

        em.close();
    }

    public List<T> list() {
        checkIfCurrentClassIsValid();
        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        List<T> resultList = em.createQuery("from " + currentClass.getSimpleName(), currentClass).getResultList();

        em.close();

        return resultList;
    }

    private void checkIfCurrentClassIsValid() {
        if (currentClass == null) {
            throw new AssertionError("In order to use this method you must set the class with setCurrentClass(). " +
                    "E.g. for UserDaoImpl extends DaoImpl<User>, you should have setCurrentClass(User.class) in your constructor.");
        }
    }

    public void add(T t) {
        EntityManager em = getEntityManager();

        em.getTransaction().begin();

        em.persist(t);

        em.close();
    }
}
