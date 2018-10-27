package asegroup1.api.daos;

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

    /**
     * A necessary parameter in many of the entityManager's methods
     */
    private Class currentClass;

    /**
     * This is not the table name in the database, it corresponds to the entity name. E.g. User
     */
    private String hibernateTableName;

    private String tableName;

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
    public List list() {
        checkIfCurrentClassIsValid();
        return entityManager.createQuery("from " + currentClass.toGenericString(), currentClass).getResultList();
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