package es.uvigo.esei.xcs.domain.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import es.uvigo.esei.xcs.domain.entities.User;

public abstract class UserService<T extends User> {

    @PersistenceContext
    protected EntityManager em;

    private final Class<T> type;

    public UserService(Class<T> type) { this.type = type; }

    public void create(T user) { em.persist(user); }
    //encuentra por login
    public Optional<T> findByLogin(String login) { return Optional.ofNullable(em.find(type, login)); }
    public List<T> findAll() { return em.createQuery("SELECT u FROM "+type.getSimpleName()+" u", type).getResultList(); }
    public void update(T user) { em.merge(user); }
    public void delete(T user) {
        if (!em.contains(user)) user = em.merge(user);
        em.remove(user);
    }
}
