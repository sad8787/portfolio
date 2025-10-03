package es.uvigo.esei.xcs.domain.services;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.domain.entities.PetVaccine;
import es.uvigo.esei.xcs.domain.entities.Vaccine;

@Stateless
public class PetVaccineService {

    @PersistenceContext
    private EntityManager em;

    /** Crear un nuevo PetVaccine */
    public void create(PetVaccine petVaccine) {
        em.persist(petVaccine);
    }

    /** Buscar por id */
    public Optional<PetVaccine> findById(int id) {
        return Optional.ofNullable(em.find(PetVaccine.class, id));
    }

    /** Listar todos los registros de vacunas aplicadas */
    public List<PetVaccine> findAll() {
        TypedQuery<PetVaccine> query = em.createQuery("SELECT pv FROM PetVaccine pv", PetVaccine.class);
        return query.getResultList();
    }

    /** Buscar todas las vacunas aplicadas a una mascota */
    public List<PetVaccine> findByPet(Pet pet) {
        TypedQuery<PetVaccine> query = em.createQuery(
            "SELECT pv FROM PetVaccine pv WHERE pv.pet = :pet", PetVaccine.class);
        query.setParameter("pet", pet);
        return query.getResultList();
    }

    /** Buscar todas las aplicaciones de una vacuna concreta */
    public List<PetVaccine> findByVaccine(Vaccine vaccine) {
        TypedQuery<PetVaccine> query = em.createQuery(
            "SELECT pv FROM PetVaccine pv WHERE pv.vaccine = :vaccine", PetVaccine.class);
        query.setParameter("vaccine", vaccine);
        return query.getResultList();
    }

    /** Buscar vacunas aplicadas en una fecha específica */
    public List<PetVaccine> findByDate(Date date) {
        TypedQuery<PetVaccine> query = em.createQuery(
            "SELECT pv FROM PetVaccine pv WHERE pv.date = :date", PetVaccine.class);
        query.setParameter("date", date);
        return query.getResultList();
    }

    /** Actualizar */
    public void update(PetVaccine petVaccine) {
        em.merge(petVaccine);
    }

    /** Eliminar */
    public void delete(PetVaccine petVaccine) {
        if (!em.contains(petVaccine)) {
            petVaccine = em.merge(petVaccine);
        }
        em.remove(petVaccine);
    }
}

