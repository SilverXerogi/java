package bookstore.dao;

import bookstore.entity.BookEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookDAO implements GenericDAO<BookEntity, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public BookEntity findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(BookEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске книги", e);
        }
    }

    @Override
    public List<BookEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM BookEntity", BookEntity.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении всех книг", e);
        }
    }

    @Override
    public BookEntity save(BookEntity book) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(book);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при сохранении книги", e);
        }
        return book;
    }

    @Override
    public BookEntity update(BookEntity book) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(book);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при обновлении книги", e);
        }
        return book;
    }

    @Override
    public void delete(Integer id) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            BookEntity book = session.get(BookEntity.class, id);
            if (book != null) {
                session.remove(book);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при удалении книги", e);
        }
    }
}