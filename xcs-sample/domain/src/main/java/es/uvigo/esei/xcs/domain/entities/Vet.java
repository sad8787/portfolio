package es.uvigo.esei.xcs.domain.entities;


import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;
import java.util.stream.Stream;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a veterinary user.
 * 
 * A Vet can attend multiple pets.
 */
@Entity
@DiscriminatorValue("VET")
@XmlRootElement(name = "vet", namespace = "http://entities.domain.xcs.esei.uvigo.es")
public class Vet extends User  {

    private static final long serialVersionUID = 1L;

    @ManyToMany(mappedBy = "vets", fetch = FetchType.EAGER)
    protected Set<Pet> pets = new HashSet<>();
    // Getter
    public Set<Pet> getPets() {
        return java.util.Collections.unmodifiableSet(pets);
    }

    // Setter
    public void setPets(Set<Pet> pets) {
        this.pets.clear();
        if (pets != null) {
            this.pets.addAll(pets);
        }
    }

    // Add helper
    public void addPet(Pet pet) {
        requireNonNull(pet, "pet can't be null");
        if (pets.add(pet)) {
            pet.getVets().add(this); // Mantener bidireccionalidad
        }
    }

    // Remove helper
    public void removePet(Pet pet) {
        requireNonNull(pet, "pet can't be null");
        if (pets.remove(pet)) {
            pet.getVets().remove(this); // Mantener bidireccionalidad
        }
    }

    

    // Required for JPA
    protected Vet() {
        super("vet@example.com", "dummy123");; // constructor JPA, valores dummy
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
   

    

    /** Internal helper to add a pet directly to the collection (no bidirectional update) */
    public void internalAddPet(Pet pet) {
        requireNonNull(pet, "pet can't be null");
        pets.add(pet);
    }

    /** Internal helper to remove a pet directly from the collection (no bidirectional update) */
    public void internalRemovePet(Pet pet) {
        requireNonNull(pet, "pet can't be null");
        pets.remove(pet);
    }

    /** Checks if a pet belongs to this vet */
    public boolean hasPet(Pet pet) {
        return pets.contains(pet);
    }
}
