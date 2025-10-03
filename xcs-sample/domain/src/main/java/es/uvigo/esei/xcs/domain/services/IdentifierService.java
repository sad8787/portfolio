package es.uvigo.esei.xcs.domain.services;

import java.util.List;
import java.util.Optional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.transaction.Transactional;

import es.uvigo.esei.xcs.domain.entities.Identifier;
import es.uvigo.esei.xcs.domain.entities.Pet;

@Transactional
public class IdentifierService {

    @PersistenceContext
    private EntityManager em;

    /**
     * Crea un nuevo identificador asociado a un Pet.
     * Valida que no exista ya un identificador con el mismo value.
     */
    public Identifier createIdentifier(String type, String value, Pet pet) {
        if (existsByValue(value)) {
            throw new IllegalArgumentException("Ya existe un identificador con el valor: " + value);
        }
        Identifier identifier = Identifier.create(type, value, pet);
        em.persist(identifier);
        return identifier;
    }

    /**
     * Encuentra un identificador por su ID.
     */
    public Optional<Identifier> findById(int id) {
        return Optional.ofNullable(em.find(Identifier.class, id));
    }

    /**
     * Encuentra un identificador por su valor único.
     */
    public Optional<Identifier> findByValue(String value) {
        TypedQuery<Identifier> query = em.createQuery(
            "SELECT i FROM Identifier i WHERE i.value = :value", Identifier.class
        );
        query.setParameter("value", value);
        List<Identifier> results = query.getResultList();
        return results.isEmpty() ? Optional.empty() : Optional.of(results.get(0));
    }

    /**
     * Comprueba si existe un identificador con un valor dado.
     */
    public boolean existsByValue(String value) {
        TypedQuery<Long> query = em.createQuery(
            "SELECT COUNT(i) FROM Identifier i WHERE i.value = :value", Long.class
        );
        query.setParameter("value", value);
        return query.getSingleResult() > 0;
    }

    /**
     * Lista todos los identificadores.
     */
    public List<Identifier> findAll() {
        return em.createQuery("SELECT i FROM Identifier i", Identifier.class)
                 .getResultList();
    }

    /**
     * Actualiza un identificador.
     */
    public Identifier update(Identifier identifier) {
        return em.merge(identifier);
    }

    /**
     * Elimina un identificador y lo desvincula de su Pet.
     */
    public void delete(Identifier identifier) {
        if (identifier == null) return;

        // Vinculación con el Pet
        Pet pet = identifier.getPet();
        if (pet != null) {
            pet.internalRemoveIdentifier(identifier);            
            identifier.setPet(null);
        }

        // Asegurarse de que está gestionado
        if (!em.contains(identifier)) {
            identifier = em.merge(identifier);
        }
        em.remove(identifier);
    }
    //Eliminar un identificador por petId + value
    public void removeIdentifierFromPet(int petId, String value) {
        Pet pet = em.find(Pet.class, petId);
        if (pet == null) {
            throw new IllegalArgumentException("Pet not found with id: " + petId);
        }

        Identifier identifier = pet.getIdentifiers().stream()
            .filter(i -> i.getValue().equals(value))
            .findFirst()
            .orElseThrow(() ->
                new IllegalArgumentException("Identifier not found with value: " + value + " for petId: " + petId)
            );

        // Desvincular correctamente
        identifier.setPet(null);

        if (!em.contains(identifier)) {
            identifier = em.merge(identifier);
        }

        em.remove(identifier);
    }
}


