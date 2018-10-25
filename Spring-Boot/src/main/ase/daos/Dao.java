package ase.daos;


import org.springframework.stereotype.Repository;

import org.springframework.transaction.annotation.Transactional;
import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

//Might be able to remove annotations from parent class, won't know until we've got hibernate up and running
@Repository
@Transactional
public interface Dao<T> {
    T get(String id);
    void delete(String id);
    List list(String tableName);
    void update(T t);
    void add(T t);
}