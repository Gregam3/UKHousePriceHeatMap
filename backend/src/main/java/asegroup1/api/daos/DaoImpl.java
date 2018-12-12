package asegroup1.api.daos;

import java.util.List;
import java.util.function.Function;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import java.util.List;
import java.util.function.Function;

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
    private EntityManager getEntityManager() {
        //On the first attempt to get an entity manager, a factory is created based on the properties in the template entity manager, entity managers are retrieved from this factory
        if (entityManagerFactory == null)
            entityManagerFactory = entityManager.getEntityManagerFactory();

        //The EntityManager will automatically be flushed on transaction completion
        return entityManagerFactory.createEntityManager();
    }
    
    public <D> D useEntityManager(Function<EntityManager, D> application) {
    	EntityManager e = getEntityManager();
    	try {
    		D data = application.apply(e);
    		e.close();
    		return data;
    	} catch(Exception ex) {
    		e.close();
    		throw ex;
    	}
    }
    
    public <D> D makeTransaction(Function<EntityManager, D> application) {
    	return useEntityManager(em -> {
    		em.getTransaction().begin();
    		D data = application.apply(em);
    		em.getTransaction().commit();
    		return data;
    	});
    }
    

    public T get(String id) {
        checkIfCurrentClassIsValid();
        return useEntityManager(em ->  {
        	return em.find(currentClass, id);
        });
	}

    public void delete(String id) {
    	makeTransaction(em -> {
    		em.remove(get(id));
    		return 0;
    	});
    }

    public void update(T t) {
    	makeTransaction(em -> {
    		em.merge(t);
    		return 0;
    	});
    }

    public List<T> list() {
        checkIfCurrentClassIsValid();
        return makeTransaction(em -> {
        	return em.createQuery("from " + currentClass.getSimpleName(), currentClass).getResultList();
        });
    }

    private void checkIfCurrentClassIsValid() {
        if (currentClass == null) {
            throw new UnsupportedOperationException("In order to use this method you must set the class with setCurrentClass(). " +
                    "E.g. for UserDaoImpl extends DaoImpl<User>, you should have setCurrentClass(User.class) in your constructor.");
        }
    }

    public void add(T t) {
    	makeTransaction(em ->{
    		em.persist(t);
    		return 0;
    	});
    }

}
