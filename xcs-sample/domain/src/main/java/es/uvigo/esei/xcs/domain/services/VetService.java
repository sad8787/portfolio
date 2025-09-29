package es.uvigo.esei.xcs.domain.services;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.domain.entities.Vet;

@Stateless
public class VetService extends UserService<Vet> {

    @PersistenceContext
    private EntityManager emInjected;

    public VetService() {
        super(Vet.class);
    }

    @PostConstruct
    private void init() {
        super.em = emInjected;
    }

    // === CRUD de Vet ===

    public void createVet(Vet vet) {
        em.persist(vet);
    }

    public Optional<Vet> findVet(String login) {
        return Optional.ofNullable(em.find(Vet.class, login));
    }

    public List<Vet> findAllVets() {
        TypedQuery<Vet> q = em.createQuery("SELECT v FROM Vet v", Vet.class);
        return q.getResultList();
    }

    public void updateVet(Vet vet) {
        em.merge(vet);
    }

    public void deleteVet(Vet vet) {
        if (!em.contains(vet)) vet = em.merge(vet);
        em.remove(vet);
    }

    // === Relación muchos a muchos Vet <-> Pet ===

    /** Listar todas las mascotas asociadas a un Vet */
    public List<Pet> getPets(Vet vet) {
        TypedQuery<Pet> q = em.createQuery(
            "SELECT p FROM Pet p JOIN p.vets v WHERE v = :vet", Pet.class
        );
        q.setParameter("vet", vet);
        return q.getResultList();
    }

    /** Añadir un Pet a un Vet */
    public void addPetToVet(Vet vet, Pet pet) {
        vet.addPet(pet);      // mantiene consistencia bidireccional
        em.merge(vet);         // sincroniza cambios en la BD
        em.merge(pet);         // opcional si pet ya estaba persistido
    }

    /** Eliminar un Pet de un Vet */
    public void removePetFromVet(Vet vet, Pet pet) {
        vet.removePet(pet);    // mantiene consistencia bidireccional
        em.merge(vet);          // sincroniza cambios
        em.merge(pet);          // opcional si pet ya estaba persistido
    }
}

