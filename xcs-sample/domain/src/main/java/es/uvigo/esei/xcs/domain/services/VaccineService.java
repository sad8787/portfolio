package es.uvigo.esei.xcs.domain.services;

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
public class VaccineService {

    @PersistenceContext
    protected  EntityManager em;

    // === CRUD de Vaccine ===

    public void create(Vaccine vaccine) {
        em.persist(vaccine);
    }

    public Optional<Vaccine> findById(int id) {
        return Optional.ofNullable(em.find(Vaccine.class, id));
    }

    public List<Vaccine> findAll() {
        TypedQuery<Vaccine> q = em.createQuery("SELECT v FROM Vaccine v", Vaccine.class);
        return q.getResultList();
    }

    public Vaccine update(Vaccine vaccine) {
        return em.merge(vaccine);
    }

    public void delete(Vaccine vaccine) {
        if (!em.contains(vaccine)) vaccine = em.merge(vaccine);
        em.remove(vaccine);
    }

    // === Consultas relacionadas con Pet ↔ Vaccine ===

    /** Buscar vacuna por nombre (único) */
    public Vaccine findByName(String name) {
        TypedQuery<Vaccine> q = em.createQuery(
            "SELECT v FROM Vaccine v WHERE v.name = :name", Vaccine.class
        );
        q.setParameter("name", name);
        List<Vaccine> results = q.getResultList();
        return results.isEmpty() ? null : results.get(0);
    }

    /** Obtener todos los Pets que recibieron una vacuna específica */
    public List<Pet> getPetsByVaccine(Vaccine vaccine) {
        TypedQuery<Pet> q = em.createQuery(
            "SELECT pv.pet FROM PetVaccine pv WHERE pv.vaccine = :vaccine", Pet.class
        );
        q.setParameter("vaccine", vaccine);
        return q.getResultList();
    }

    /** Obtener todas las PetVaccine asociadas a una vacuna */
    public List<PetVaccine> getPetVaccines(Vaccine vaccine) {
        TypedQuery<PetVaccine> q = em.createQuery(
            "SELECT pv FROM PetVaccine pv WHERE pv.vaccine = :vaccine", PetVaccine.class
        );
        q.setParameter("vaccine", vaccine);
        return q.getResultList();
    }

    /** Eliminar relación PetVaccine específica (Pet ya vacunado) */
    public void removePetVaccine(PetVaccine pv) {
        Pet pet = pv.getPet();
        Vaccine vaccine = pv.getVaccine();

        pet.internalRemovePetVaccine(pv);
        vaccine.internalRemovePetVaccine(pv);

        if (!em.contains(pv)) pv = em.merge(pv);
        em.remove(pv);

        em.merge(pet);
        em.merge(vaccine);
    }
}


