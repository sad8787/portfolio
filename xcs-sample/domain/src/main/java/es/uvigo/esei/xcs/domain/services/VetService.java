package es.uvigo.esei.xcs.domain.services;

import es.uvigo.esei.xcs.domain.entities.Pet;
import es.uvigo.esei.xcs.domain.entities.Vet;

import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import java.util.List;

public class VetService extends UserService<Vet> {

    public VetService(EntityManager em) {
        super(em, Vet.class);
    }

    /**
     * Returns the pets attended by a given vet.
     * @param vet the vet (not null)
     * @return list of pets attended by the vet
     */
    public List<Pet> getPetsAttended(Vet vet) {
        TypedQuery<Pet> query = em.createQuery(
                "SELECT p FROM Pet p WHERE p.vet = :vet", Pet.class
        );
        query.setParameter("vet", vet);
        return query.getResultList();
    }

    /**
     * Finds all vets attending exactly 'count' number of pets.
     * @param count the number of pets
     * @return list of vets with exactly 'count' pets
     */
    public List<Vet> findByNumberOfPets(int count) {
        TypedQuery<Vet> query = em.createQuery(
                "SELECT v FROM Vet v WHERE SIZE(v.pets) = :count", Vet.class
        );
        query.setParameter("count", count);
        return query.getResultList();
    }

    /**
     * Finds all vets attending at least 'minCount' pets.
     * @param minCount minimum number of pets
     * @return list of vets with at least 'minCount' pets
     */
    public List<Vet> findByMinimumPets(int minCount) {
        TypedQuery<Vet> query = em.createQuery(
                "SELECT v FROM Vet v WHERE SIZE(v.pets) >= :minCount", Vet.class
        );
        query.setParameter("minCount", minCount);
        return query.getResultList();
    }
}
