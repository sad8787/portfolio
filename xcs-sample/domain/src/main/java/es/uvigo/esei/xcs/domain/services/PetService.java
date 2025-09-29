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
import es.uvigo.esei.xcs.domain.entities.Vet;

@Stateless
public class PetService {

    @PersistenceContext
    private EntityManager em;

    // === CRUD de Pet ===

    public void createPet(Pet pet) {
        em.persist(pet);
    }

    public Optional<Pet> findPet(int id) {
        return Optional.ofNullable(em.find(Pet.class, id));
    }

    public List<Pet> findAllPets() {
        TypedQuery<Pet> q = em.createQuery("SELECT p FROM Pet p", Pet.class);
        return q.getResultList();
    }

    public void updatePet(Pet pet) {
        em.merge(pet);
    }

    public void deletePet(Pet pet) {
        if (!em.contains(pet)) pet = em.merge(pet);
        em.remove(pet);
    }

    // === Relación Pet ↔ Vet (muchos a muchos) ===

    public List<Vet> getVets(Pet pet) {
        TypedQuery<Vet> q = em.createQuery(
            "SELECT v FROM Vet v JOIN v.pets p WHERE p = :pet", Vet.class
        );
        q.setParameter("pet", pet);
        return q.getResultList();
    }

    public void addVetToPet(Pet pet, Vet vet) {
        pet.addVet(vet);   // mantener consistencia bidireccional
        em.merge(pet);
        em.merge(vet);
    }

    public void removeVetFromPet(Pet pet, Vet vet) {
        pet.removeVet(vet);  // mantener consistencia bidireccional
        em.merge(pet);
        em.merge(vet);
    }

    // === Relación Pet ↔ PetVaccine ↔ Vaccine ===

    public void addVaccine(Pet pet, Vaccine vaccine, Date date) {
        PetVaccine pv = new PetVaccine(pet, vaccine, date);
        em.persist(pv);
        em.merge(pet);
        em.merge(vaccine);
    }

    public List<Vaccine> getVaccines(Pet pet) {
        TypedQuery<Vaccine> q = em.createQuery(
            "SELECT pv.vaccine FROM PetVaccine pv WHERE pv.pet = :pet", Vaccine.class
        );
        q.setParameter("pet", pet);
        return q.getResultList();
    }

    public void removeVaccine(Pet pet, Vaccine vaccine) {
        TypedQuery<PetVaccine> q = em.createQuery(
            "SELECT pv FROM PetVaccine pv WHERE pv.pet = :pet AND pv.vaccine = :vaccine", PetVaccine.class
        );
        q.setParameter("pet", pet);
        q.setParameter("vaccine", vaccine);
        List<PetVaccine> pvs = q.getResultList();

        for (PetVaccine pv : pvs) {
            pet.internalRemovePetVaccine(pv);
            vaccine.internalRemovePetVaccine(pv);
            if (!em.contains(pv)) pv = em.merge(pv);
            em.remove(pv);
        }

        em.merge(pet);
        em.merge(vaccine);
    }
}

