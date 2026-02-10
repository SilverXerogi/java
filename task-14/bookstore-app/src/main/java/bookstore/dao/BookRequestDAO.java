package bookstore.dao;

import bookstore.entity.BookRequestEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import java.util.List;

public class BookRequestDAO implements GenericDAO<BookRequestEntity, Integer> {
    private final SessionFactory sessionFactory;

    public BookRequestDAO(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public BookRequestEntity findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(BookRequestEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске заявки", e);
        }
    }

    @Override
    public List<BookRequestEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM BookRequestEntity", BookRequestEntity.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении всех заявок", e);
        }
    }

    @Override
    public BookRequestEntity save(BookRequestEntity request) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(request);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при сохранении заявки", e);
        }
        return request;
    }

    @Override
    public BookRequestEntity update(BookRequestEntity request) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(request);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при обновлении заявки", e);
        }
        return request;
    }

    @Override
    public void delete(Integer id) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            BookRequestEntity request = session.get(BookRequestEntity.class, id);
            if (request != null) {
                session.remove(request);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при удалении заявки", e);
        }
    }
}