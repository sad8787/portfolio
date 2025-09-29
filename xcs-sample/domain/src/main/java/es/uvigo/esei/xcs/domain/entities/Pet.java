package es.uvigo.esei.xcs.domain.entities;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import static java.util.Collections.unmodifiableCollection;
import java.util.Date;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import static org.apache.commons.lang3.Validate.inclusiveBetween;

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

    @ManyToMany
    @JoinTable(
    name = "pet_vet",
    joinColumns = @JoinColumn(name = "pet_id", referencedColumnName = "id"),
    inverseJoinColumns = @JoinColumn(name = "vet_login", referencedColumnName = "login")
    )
    @XmlTransient
    protected  Set<Vet> vets = new HashSet<>();

    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    protected Set<Identifier> identifiers = new HashSet<>();

    

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
    //ownwr
    public Owner getOwner() { return owner; }
    public void setOwner(Owner owner) {
        if (this.owner != null) this.owner.internalRemovePet(this);
        this.owner = owner;
        if (this.owner != null) this.owner.internalAddPet(this);
    }
    
    
    
    //vet
        // Getter
    public Set<Vet> getVets() {
        return Collections.unmodifiableSet(vets);
    }

    // Setter (reemplaza la colección completa)
    public void setVets(Set<Vet> vets) {
        this.vets.clear();
        if (vets != null) {
            this.vets.addAll(vets);
        }
    }

    // Métodos auxiliares para mantener consistencia
    public void addVet(Vet vet) {
        requireNonNull(vet, "vet can't be null");
        this.vets.add(vet);
        vet.getPets().add(this);
    }

    public void removeVet(Vet vet) {
        requireNonNull(vet, "vet can't be null");
        this.vets.remove(vet);
        vet.getPets().remove(this);
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
    @OneToMany(mappedBy = "pet", cascade = CascadeType.ALL, orphanRemoval = true)
    private final Set<PetVaccine> petVaccines = new HashSet<>();


    // Getter público
    public Set<PetVaccine> getPetVaccines() {
        return Collections.unmodifiableSet(petVaccines);
    }

    // Métodos internos para mantener consistencia bidireccional
    public void internalAddPetVaccine(PetVaccine pv) {
        requireNonNull(pv, "petVaccine can't be null");
        this.petVaccines.add(pv);
        if (pv.getPet() != this) {
            pv.setPet(this);
        }
    }

    public void internalRemovePetVaccine(PetVaccine pv) {
        requireNonNull(pv, "petVaccine can't be null");
        this.petVaccines.remove(pv);
        if (pv.getPet() == this) {
            pv.setPet(null);
        }
    }


}








