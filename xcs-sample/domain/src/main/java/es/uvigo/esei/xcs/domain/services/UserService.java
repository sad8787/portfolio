package es.uvigo.esei.xcs.domain.services;

import es.uvigo.esei.xcs.domain.entities.User;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.TypedQuery;
import java.util.List;
import java.util.Optional;

public class UserService<T extends User> {

    protected final EntityManager em;
    private final Class<T> type;

    public UserService(EntityManager em, Class<T> type) {
        this.em = em;
        this.type = type;
    }

    /** Create a new user */
    public void create(T user) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.persist(user);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /** Read by login (primary key) */
    public Optional<T> findByLogin(String login) {
        T user = em.find(type, login);
        return Optional.ofNullable(user);
    }

    /** Read all users */
    public List<T> findAll() {
        TypedQuery<T> query = em.createQuery("SELECT u FROM " + type.getSimpleName() + " u", type);
        return query.getResultList();
    }

    /** Update a user */
    public void update(T user) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(user);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }

    /** Delete a user */
    public void delete(T user) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            if (!em.contains(user)) user = em.merge(user);
            em.remove(user);
            tx.commit();
        } catch (RuntimeException e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        }
    }
}

