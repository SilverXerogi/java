package bookstore.dao;

import java.util.List;

public interface GenericDAO<T, ID> {
    T findById(ID id);
    List<T> findAll();
    T save(T entity);
    T update(T entity);
    void delete(ID id);
}