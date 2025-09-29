package es.uvigo.esei.xcs.domain.services;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import es.uvigo.esei.xcs.domain.entities.Owner;
import es.uvigo.esei.xcs.domain.entities.Pet;

@Stateless
public class OwnerService {

    @PersistenceContext
    private EntityManager em;

    // ============ CRUD básico ============

    public Owner createOwner(Owner owner) {
        em.persist(owner);
        return owner;
    }

    public Optional<Owner> findOwner(int id) {
        return Optional.ofNullable(em.find(Owner.class, id));
    }

    public List<Owner> listOwners() {
        return em.createQuery("SELECT o FROM Owner o", Owner.class)
                 .getResultList();
    }

    public Owner updateOwner(Owner owner) {
        return em.merge(owner);
    }

    public void deleteOwner(int id) {
        Owner owner = em.find(Owner.class, id);
        if (owner != null) {
            em.remove(owner);
        }
    }

    // ============ Gestión de mascotas ============

    public List<Pet> listPets(int ownerId) {
        Owner owner = em.find(Owner.class, ownerId);
        if (owner == null) {
            throw new IllegalArgumentException("Owner not found with id: " + ownerId);
        }
        return new ArrayList<>(owner.getPets()); // devuelve copia 
    }


    public void addPetToOwner(int ownerId, Pet pet) {
        Owner owner = em.find(Owner.class, ownerId);
        if (owner == null) {
            throw new IllegalArgumentException("Owner not found with id: " + ownerId);
        }

        // si el pet ya tenía un owner distinto, lo eliminamos de ese owner
        Owner currentOwner = pet.getOwner();
        if (currentOwner != null && !currentOwner.equals(owner)) {
            currentOwner.removePet(pet);  // mantiene consistencia bidireccional
        }

        // asignamos el nuevo owner
        owner.addPet(pet);

        // persistimos solo si es nuevo
        if (pet.getId() == 0) {
            em.persist(pet);
        } else {
            em.merge(pet);
        }
    }

    public void removePetFromOwner(int ownerId, int petId) {
        Owner owner = em.find(Owner.class, ownerId);
        if (owner == null) {
            throw new IllegalArgumentException("Owner not found with id: " + ownerId);
        }

        Pet pet = em.find(Pet.class, petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found with id: " + petId);
        }

        if (!owner.ownsPet(pet)) {
            throw new IllegalArgumentException("This pet does not belong to the owner");
        }

        // Rompe la relación pero no borra al pet
        owner.removePet(pet);
        em.merge(owner); //  asegura sincronización en BD
        em.merge(pet);   //  asegura que pet.owner = null se guarde
    }

}

