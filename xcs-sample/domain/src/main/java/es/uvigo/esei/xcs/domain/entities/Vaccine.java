package es.uvigo.esei.xcs.domain.entities;

import java.io.Serializable;
import java.util.HashSet;
import static java.util.Objects.requireNonNull;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;


/**
 * Clase base para las vacunas.
 */
@Entity
@Table(name = "VACCINE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "VACCINE_TYPE", discriminatorType = DiscriminatorType.STRING)
@XmlRootElement(name = "vaccine", namespace = "http://entities.domain.xcs.esei.uvigo.es")
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class Vaccine implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(length = 100, nullable = false, unique = true)
    private String name;

    @OneToMany(mappedBy = "vaccine", cascade = CascadeType.ALL, orphanRemoval = true)
    private final  Set<PetVaccine> petVaccines = new HashSet<>();

    public Set<PetVaccine> getPetVaccines() {
        return java.util.Collections.unmodifiableSet(petVaccines);
    }

    public void internalAddPetVaccine(PetVaccine pv) {
        requireNonNull(pv, "petVaccine can't be null");
        petVaccines.add(pv);
        if (pv.getVaccine() != this) {
            pv.setVaccine(this);
        }
    }

    public void internalRemovePetVaccine(PetVaccine pv) {
        requireNonNull(pv, "petVaccine can't be null");
        petVaccines.remove(pv);
        if (pv.getVaccine() == this) {
            pv.setVaccine(null);
        }
    }

    // Constructor requerido por JPA
    protected Vaccine() {}

    
    public Vaccine(String name) {
        requireNonNull(name, "name cannot be null");
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        this.name = name;
    }

    public void setName(String name) {
        requireNonNull(name, "name cannot be null");
        if (name.trim().isEmpty()) throw new IllegalArgumentException("name cannot be empty");
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

   
}
