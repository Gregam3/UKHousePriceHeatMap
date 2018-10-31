package asegroup1.api.daos;

import java.util.List;

/**
 * @author Greg Mitten
 * gregoryamitten@gmail.com
 */

public interface Dao<T> {
	T get(String id);

	void delete(String id);

	List list();

	void update(T t);

	void add(T t);
}
