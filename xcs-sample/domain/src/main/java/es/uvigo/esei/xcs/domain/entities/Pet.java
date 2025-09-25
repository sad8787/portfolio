package es.uvigo.esei.xcs.domain.entities;

import static java.util.Objects.requireNonNull;
import static org.apache.commons.lang3.Validate.inclusiveBetween;
import static java.util.Collections.unmodifiableCollection;
import static java.util.Collections.unmodifiableSet;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

import javax.persistence.*;

/**
 * A pet.
 * 
 * @author Miguel Reboiro-Jato
 */
@Entity(name = "Pet")
@XmlRootElement(name = "pet", namespace = "http://entities.domain.xcs.esei.uvigo.es")
@XmlAccessorType(XmlAccessType.FIELD)
public class Pet implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 100, nullable = false)
    private String name;

    @Column(nullable = false, length = 4)
    @Enumerated(EnumType.STRING)
    private AnimalType animal;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date birth;

    @ManyToOne
    @JoinColumn(name = "owner", referencedColumnName = "login", nullable = false)
    @XmlTransient
    private Owner owner;

    @ManyToOne
    @JoinColumn(name = "vet", referencedColumnName = "login", nullable = true)
    @XmlTransient
    private Vet vet;

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<Identifier> identifiers = new HashSet<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<PetVaccine> petVaccines = new HashSet<>();

    // Required for JPA
    protected Pet() {}

    // Constructors
    public Pet(String name, AnimalType animal, Date birth) {
        this(name, animal, birth, null, null);
    }

    public Pet(String name, AnimalType animal, Date birth, Owner owner) {
        this(name, animal, birth, owner, null);
    }

    public Pet(String name, AnimalType animal, Date birth, Vet vet) {
        this(name, animal, birth, null, vet);
    }

    public Pet(String name, AnimalType animal, Date birth, Owner owner, Vet vet) {
        this.setName(name);
        this.setAnimal(animal);
        this.setBirth(birth);
        this.setOwner(owner);
        this.setVet(vet);
    }

    // Getters & Setters
    public int getId() { return id; }

    public String getName() { return name; }
    public void setName(String name) {
        requireNonNull(name, "name can't be null");
        inclusiveBetween(1, 100, name.length(), "name must have a length between 1 and 100");
        this.name = name;
    }

    public AnimalType getAnimal() { return animal; }
    public void setAnimal(AnimalType animal) {
        requireNonNull(animal, "animal can't be null");
        this.animal = animal;
    }

    public Date getBirth() { return birth; }
    public void setBirth(Date birth) {
        requireNonNull(birth, "birth can't be null");
        inclusiveBetween(new Date(0), new Date(), birth, "birth must be previous to the current time");
        this.birth = birth;
    }

    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) {
        if (this.owner != null) this.owner.internalRemovePet(this);
        this.owner = owner;
        if (this.owner != null) this.owner.internalAddPet(this);
    }

    public Vet getVet() { return vet; }
    public void setVet(Vet vet) {
        if (this.vet != null) this.vet.internalRemovePet(this);
        this.vet = vet;
        if (this.vet != null) this.vet.internalAddPet(this);
    }

    // Identifiers
    public Collection<Identifier> getIdentifiers() {
        return unmodifiableCollection(identifiers);
    }
    void internalAddIdentifier(Identifier identifier) {
        requireNonNull(identifier, "identifier can't be null");
        this.identifiers.add(identifier);
    }
    void internalRemoveIdentifier(Identifier identifier) {
        requireNonNull(identifier, "identifier can't be null");
        this.identifiers.remove(identifier);
    }

    // PetVaccine
    public Set<PetVaccine> getPetVaccines() {
        return unmodifiableSet(petVaccines);
    }
    void internalAddPetVaccine(PetVaccine petVaccine) {
        requireNonNull(petVaccine, "petVaccine can't be null");
        this.petVaccines.add(petVaccine);
    }
    void internalRemovePetVaccine(PetVaccine petVaccine) {
        requireNonNull(petVaccine, "petVaccine can't be null");
        this.petVaccines.remove(petVaccine);
    }
}








