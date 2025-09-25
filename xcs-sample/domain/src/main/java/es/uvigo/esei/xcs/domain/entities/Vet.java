package es.uvigo.esei.xcs.domain.entities;

import static java.util.Objects.requireNonNull;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a veterinary user.
 * 
 * A Vet can attend multiple pets.
 */
@Entity
@DiscriminatorValue("VET")
@XmlRootElement(name = "vet", namespace = "http://entities.domain.xcs.esei.uvigo.es")
public class Vet extends User implements Serializable {

    private static final long serialVersionUID = 1L;

    @OneToMany(
        mappedBy = "vet",
        cascade = CascadeType.ALL,
        orphanRemoval = true,
        fetch = FetchType.EAGER
    )
    private Set<Pet> pets = new HashSet<>();

    // Required for JPA
    protected Vet() {
        super("dummy", "dummy123"); // constructor JPA, valores dummy
    }

    /** 
     * Creates a new Vet without pets.
     */
    public Vet(String login, String password) {
        super(login, password);
    }

    /** 
     * Creates a new Vet with initial pets.
     */
    public Vet(String login, String password, Pet... pets) {
        this(login, password);
        Stream.of(pets).forEach(this::addPet);
    }

    /** Returns an unmodifiable collection of pets attended by this vet */
    public Collection<Pet> getPets() {
        return java.util.Collections.unmodifiableCollection(pets);
    }

    /** Adds a pet to this vet */
    public void addPet(Pet pet) {
        requireNonNull(pet, "pet can't be null");

        if (!pets.contains(pet)) {
            pets.add(pet);
            pet.setVet(this);
        }
    }

    /** Removes a pet from this vet */
    public void removePet(Pet pet) {
        requireNonNull(pet, "pet can't be null");

        if (pets.contains(pet)) {
            pets.remove(pet);
            pet.setVet(null);
        } else {
            throw new IllegalArgumentException("pet doesn't belong to this vet");
        }
    }

    /** Internal helper to add a pet directly to the collection (no bidirectional update) */
    void internalAddPet(Pet pet) {
        requireNonNull(pet, "pet can't be null");
        pets.add(pet);
    }

    /** Internal helper to remove a pet directly from the collection (no bidirectional update) */
    void internalRemovePet(Pet pet) {
        requireNonNull(pet, "pet can't be null");
        pets.remove(pet);
    }

    /** Checks if a pet belongs to this vet */
    public boolean hasPet(Pet pet) {
        return pets.contains(pet);
    }
}
