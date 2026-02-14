package bookstore.dao;

import bookstore.entity.OrderEntity;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class OrderDAO implements GenericDAO<OrderEntity, Integer> {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public OrderEntity findById(Integer id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(OrderEntity.class, id);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при поиске заказа", e);
        }
    }

    @Override
    public List<OrderEntity> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM OrderEntity", OrderEntity.class).list();
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при получении всех заказов", e);
        }
    }

    @Override
    public OrderEntity save(OrderEntity order) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.persist(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при сохранении заказа", e);
        }
        return order;
    }

    @Override
    public OrderEntity update(OrderEntity order) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            session.merge(order);
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при обновлении заказа", e);
        }
        return order;
    }

    @Override
    public void delete(Integer id) {
        Transaction tx = null;
        try (Session session = sessionFactory.openSession()) {
            tx = session.beginTransaction();
            OrderEntity order = session.get(OrderEntity.class, id);
            if (order != null) {
                session.remove(order);
            }
            tx.commit();
        } catch (Exception e) {
            if (tx != null) tx.rollback();
            throw new RuntimeException("Ошибка при удалении заказа", e);
        }
    }
}